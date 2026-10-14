package wtf.gemini.register;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import wtf.gemini.Module;
import wtf.gemini.hud.ArmorHUD;
import wtf.gemini.hud.CoinRate;
import wtf.gemini.hud.HudStyle;
import wtf.gemini.hud.InfoPanel;
import wtf.gemini.hud.KeyStrokes;
import wtf.gemini.hud.TargetHUD;
import wtf.gemini.hud.WaterMark;
import wtf.gemini.modules.impl.movement.Sprint;
import wtf.gemini.modules.impl.player.AutoFish;
import wtf.gemini.modules.impl.player.ItemScroller;
import wtf.gemini.modules.impl.player.ItemSwap;
import wtf.gemini.modules.impl.render.FullBright;
import wtf.gemini.modules.impl.render.HitMarker;
import wtf.gemini.modules.impl.render.ScoreboardHP;
import wtf.gemini.modules.impl.render.SeeInvisible;
import wtf.gemini.modules.impl.render.ShulkerPreview;
import wtf.gemini.modules.impl.render.TalismanHighlight;
import wtf.gemini.modules.impl.render.Theme;
import wtf.gemini.modules.impl.render.TotemPopSound;
import wtf.gemini.modules.impl.render.TargetEsp;
import wtf.gemini.modules.impl.render.Trajectories;
import wtf.gemini.modules.impl.render.Zoom;
import wtf.gemini.util.Projection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ModuleStorage {
    private final List<Module> modules = new ArrayList<>();
    private List<Module> visibleCache;

    public void init() {
        register(new AutoFish());
        register(new ItemScroller());
        register(new ScoreboardHP());
        register(new FullBright());
        register(new ShulkerPreview());
        register(new HitMarker());
        register(new TotemPopSound());
        register(new TalismanHighlight());
        register(new Sprint());
        register(new KeyStrokes());
        register(new WaterMark());
        register(new InfoPanel());
        register(new TargetHUD());
        register(new ArmorHUD());
        register(new HudStyle());
        register(new CoinRate());
        register(new Theme());
        register(new Zoom());
        register(new ItemSwap());
        register(new Trajectories());
        register(new TargetEsp());
        register(new SeeInvisible());

        // Включены по умолчанию (пассивные / HUD)
        get(ItemScroller.class).setEnabled(true);
        get(ShulkerPreview.class).setEnabled(true);
        get(HitMarker.class).setEnabled(true);
        get(TotemPopSound.class).setEnabled(true);
        get(TalismanHighlight.class).setEnabled(true);
        get(WaterMark.class).setEnabled(true);
        get(InfoPanel.class).setEnabled(true);
        get(TargetHUD.class).setEnabled(true);
        get(ArmorHUD.class).setEnabled(true);
        get(HudStyle.class).setEnabled(true);
        // Вне HolyWorld модуль молчит: запросы к REST API уходят только на их серверах.
        get(CoinRate.class).setEnabled(true);
        // Тема применяется постоянно, поэтому включена всегда — иначе цвета «замерзали» бы.
        get(Theme.class).setEnabled(true);
        get(Zoom.class).setEnabled(true);
        // ItemSwap — модуль-действие: он включён и ждёт нажатия своей клавиши.
        get(ItemSwap.class).setEnabled(true);
        get(Trajectories.class).setEnabled(true);
        get(TargetEsp.class).setEnabled(true);
    }

    private void register(Module module) {
        module.registerKeyBinding();
        modules.add(module);
    }

    public void tick() {
        if (MinecraftClient.getInstance().player == null) return;
        for (Module m : modules) {
            // Скрытая функция не должна реагировать ни на клавишу, ни на тик.
            if (m.isBlocked()) continue;
            handleKey(m);
            if (m.isEnabled()) m.onTick();
        }
    }

    /**
     * Клавиша модуля: удерживаемая включает модуль на время нажатия, клавиша-действие
     * выполняет действие, обычная — переключает состояние.
     */
    private void handleKey(Module m) {
        if (m.isActionModule()) {
            while (m.getKeyBinding().wasPressed()) {
                if (m.isEnabled()) m.activate();
            }
        } else if (m.isHoldKey()) {
            m.setEnabled(m.getKeyBinding().isPressed(), false);
        } else {
            while (m.getKeyBinding().wasPressed()) m.toggle();
        }
    }

    public void renderHud(DrawContext context) {
        if (MinecraftClient.getInstance().options.hudHidden) return;
        for (Module m : getVisibleModules()) {
            if (m.isEnabled()) m.onHudRender(context);
        }
    }

    /** Отрисовка модулей в мире. Подписана на {@code WorldRenderEvents.LAST}. */
    public void renderWorld(WorldRenderContext context) {
        // Матрицы кадра нужны всем, кто проецирует мир в экранные координаты: рамки, метки.
        Projection.capture(context);
        for (Module m : getVisibleModules()) {
            if (m.isEnabled()) m.onWorldRender(context);
        }
    }

    /**
     * Модули, которые можно показывать игроку: без скрытых сервером.
     *
     * <p>Список кэшируется: вызывается каждый кадр из отрисовки HUD, а аллокации в кадре
     * здесь не приветствуются. Кэш сбрасывается при изменении блок-листа.
     */
    public List<Module> getVisibleModules() {
        List<Module> cached = visibleCache;
        if (cached == null) {
            cached = new ArrayList<>(modules.size());
            for (Module m : modules) {
                if (!m.isBlocked()) cached.add(m);
            }
            visibleCache = cached;
        }
        return cached;
    }

    /** Сбрасывает кэш видимых модулей. Вызывается после изменения блок-листа сервера. */
    public void invalidateVisible() {
        visibleCache = null;
    }

    /** Все модули, включая скрытые — для конфига и служебных обходов. */
    public List<Module> getModules() {
        return modules;
    }

    /** Все идентификаторы функций для запроса {@code checkFeatures} (без повторов). */
    public Set<String> getFeatureIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (Module m : modules) {
            ids.add(m.getFeatureId());
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        T module = find(type);
        if (module == null) {
            throw new IllegalStateException("Module not registered: " + type.getName());
        }
        return module;
    }

    /**
     * Безопасный поиск модуля.
     *
     * @return модуль или {@code null}, если он ещё не зарегистрирован
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> T find(Class<T> type) {
        for (Module m : modules) {
            if (m.getClass() == type) return (T) m;
        }
        return null;
    }
}
