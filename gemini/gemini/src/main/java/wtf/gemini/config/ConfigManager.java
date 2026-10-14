package wtf.gemini.config;

import net.fabricmc.loader.api.FabricLoader;
import wtf.gemini.Gemini;
import wtf.gemini.Module;
import wtf.gemini.hud.HudModule;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.setting.ColorSetting;
import wtf.gemini.setting.FloatSetting;
import wtf.gemini.setting.ModeSetting;
import wtf.gemini.setting.Setting;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/** Сохраняет включённость модулей, значения настроек и позиции HUD в config/gemini.properties. */
public final class ConfigManager {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("gemini.properties");
    /** Периодичность сброса отложенных изменений на диск (~10 секунд). */
    private static final int AUTOSAVE_TICKS = 200;

    private static boolean dirty;
    private static int tickCounter;

    private ConfigManager() {}

    private static String key(Module m, String suffix) {
        return m.getName().replaceAll("[^A-Za-z0-9]", "_") + "." + suffix;
    }

    private static String settingKey(Module m, Setting s) {
        return key(m, "s." + s.getName().replaceAll("[^A-Za-z0-9]", "_"));
    }

    /** Помечает конфиг изменённым. Запись на диск произойдёт не сразу, а при следующем сбросе. */
    public static void markDirty() {
        dirty = true;
    }

    /** Вызывается каждый клиентский тик: раз в ~10 секунд сбрасывает отложенные изменения. */
    public static void tick() {
        if (++tickCounter < AUTOSAVE_TICKS) {
            return;
        }
        tickCounter = 0;
        saveIfDirty();
    }

    public static void saveIfDirty() {
        if (dirty) {
            save();
        }
    }

    public static void load(List<Module> modules) {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            Gemini.LOGGER.warn("[Gemini] Cannot read config: {}", e.getMessage());
            return;
        }

        for (Module m : modules) {
            String en = p.getProperty(key(m, "enabled"));
            if (en != null) m.setEnabled(Boolean.parseBoolean(en));

            for (Setting s : m.getSettings()) {
                String v = p.getProperty(settingKey(m, s));
                if (v == null) continue;
                try {
                    if (s instanceof BooleanSetting b) b.set(Boolean.parseBoolean(v));
                    else if (s instanceof FloatSetting f) f.set(Float.parseFloat(v));
                    else if (s instanceof ColorSetting c) c.fromHex(v);
                    else if (s instanceof ModeSetting mo) mo.set(v);
                } catch (NumberFormatException ignored) {
                    // битое значение — остаётся по умолчанию
                }
            }

            if (m instanceof HudModule h) {
                String xs = p.getProperty(key(m, "x")), ys = p.getProperty(key(m, "y"));
                if (xs != null && ys != null) {
                    try {
                        h.setPos(Integer.parseInt(xs), Integer.parseInt(ys));
                    } catch (NumberFormatException ignored) {
                        // позиция по умолчанию
                    }
                }
            }
        }
    }

    public static void save() {
        if (Gemini.INSTANCE == null || Gemini.INSTANCE.moduleStorage == null) return;
        Properties p = new Properties();
        for (Module m : Gemini.INSTANCE.moduleStorage.getModules()) {
            // Для скрытого сервером модуля пишем намерение игрока, а не принудительное «выключено».
            p.setProperty(key(m, "enabled"), Boolean.toString(m.getPersistedEnabled()));
            for (Setting s : m.getSettings()) {
                if (s instanceof BooleanSetting b) p.setProperty(settingKey(m, s), Boolean.toString(b.get()));
                else if (s instanceof FloatSetting f) p.setProperty(settingKey(m, s), Float.toString(f.get()));
                else if (s instanceof ColorSetting c) p.setProperty(settingKey(m, s), c.toHex());
                else if (s instanceof ModeSetting mo) p.setProperty(settingKey(m, s), mo.get());
            }
            if (m instanceof HudModule h && h.isPlaced()) {
                p.setProperty(key(m, "x"), Integer.toString(h.getX()));
                p.setProperty(key(m, "y"), Integer.toString(h.getY()));
            }
        }
        try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
            p.store(w, "Gemini Client config");
            dirty = false;
        } catch (IOException e) {
            Gemini.LOGGER.warn("[Gemini] Cannot save config: {}", e.getMessage());
        }
    }
}
