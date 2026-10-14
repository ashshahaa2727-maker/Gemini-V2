package wtf.gemini;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import wtf.gemini.setting.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class Module {
    public enum Category { PLAYER, RENDER, MOVEMENT, HUD }

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final Category category;
    private final String description;
    private final String featureId;
    private final List<Setting> settings = new ArrayList<>();
    private boolean enabled;
    private boolean blocked;
    private boolean enabledBeforeBlock;
    private KeyBinding keyBinding;

    protected Module(String name, Category category, String description) {
        this(name, category, description, defaultFeatureId(name));
    }

    /**
     * @param featureId идентификатор функции для LiteAPI; задаётся явно, если модуль
     *                  соответствует известной функции сервера (например {@code fullbright})
     */
    protected Module(String name, Category category, String description, String featureId) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.featureId = featureId;
    }

    /**
     * Идентификатор функции по умолчанию: имя модуля в нижнем регистре без разделителей.
     *
     * <p>Сервер оперирует своими именами функций, поэтому модуль, у которого есть
     * общепринятый аналог, должен передать {@code featureId} в конструктор.
     */
    public static String defaultFeatureId(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    /** Регистрирует настройки модуля (они появятся в меню по ПКМ и сохранятся в конфиг). */
    protected void addSettings(Setting... values) {
        for (Setting setting : values) {
            settings.add(setting);
        }
    }

    public List<Setting> getSettings() {
        return settings;
    }

    /**
     * Клавиша по умолчанию из настроек управления.
     *
     * @return код клавиши GLFW либо {@link GLFW#GLFW_KEY_UNKNOWN}, если назначать не нужно
     */
    public int getDefaultKey() {
        return GLFW.GLFW_KEY_UNKNOWN;
    }

    /** Переключатель в настройках управления. */
    public void registerKeyBinding() {
        this.keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gemini." + name.toLowerCase(Locale.ROOT),
                InputUtil.Type.KEYSYM,
                getDefaultKey(),
                "key.categories.gemini"));
    }

    /**
     * Если true, клавиша работает как «удерживать»: модуль включён, пока её держат.
     * Так устроен зум — как в OptiFine.
     */
    public boolean isHoldKey() {
        return false;
    }

    /**
     * Если true, клавиша не переключает модуль, а выполняет {@link #activate()},
     * пока модуль включён; состояние в меню при этом не меняется.
     */
    public boolean isActionModule() {
        return false;
    }

    /** Действие модуля-действия. Вызывается по нажатию его клавиши. */
    public void activate() {
    }

    public KeyBinding getKeyBinding() {
        return keyBinding;
    }

    public void setEnabled(boolean value) {
        setEnabled(value, true);
    }

    /**
     * @param notify показывать ли уведомление. Для скрытой функции всегда {@code false}:
     *               игрок не должен видеть даже упоминания о ней.
     */
    public void setEnabled(boolean value, boolean notify) {
        if (this.enabled == value) {
            return;
        }
        this.enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
        if (notify && !blocked) {
            Gemini.notifyPlayer(name + (value ? ": ON" : ": OFF"));
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Скрывает модуль по требованию сервера.
     *
     * <p>Модуль принудительно выключается без уведомления, а интерфейс перестаёт его показывать
     * (см. {@link wtf.gemini.register.ModuleStorage#getVisibleModules()}). Состояние «до блокировки»
     * запоминается, чтобы вернуть его, когда сервер снимет блокировку.
     */
    public void setBlocked(boolean value) {
        if (this.blocked == value) {
            return;
        }
        this.blocked = value;
        if (value) {
            this.enabledBeforeBlock = this.enabled;
            setEnabled(false, false);
        } else if (this.enabledBeforeBlock) {
            setEnabled(true, false);
        }
    }

    public boolean isBlocked() {
        return blocked;
    }

    /**
     * Состояние для конфига: у скрытого модуля сохраняем намерение игрока,
     * а не принудительное «выключено», иначе настройка терялась бы после блокировки.
     */
    public boolean getPersistedEnabled() {
        return blocked ? enabledBeforeBlock : enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getFeatureId() {
        return featureId;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    /** Каждый клиентский тик, пока модуль включён. */
    public void onTick() {
    }

    /** Отрисовка HUD, пока модуль включён. */
    public void onHudRender(DrawContext context) {
    }

    /**
     * Отрисовка в мире (3D), пока модуль включён.
     *
     * <p>Вызывается из {@code WorldRenderEvents.LAST}: матрица уже сдвинута на камеру,
     * а буфер отрисовки берётся из контекста — свой шейдер заводить не нужно.
     */
    public void onWorldRender(WorldRenderContext context) {
    }
}
