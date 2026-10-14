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
import wtf.gemini.hud.PotionsHUD;
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
        register(new PotionsHUD());

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
        get(CoinRate.class).setEnabled(true);
        get(Theme.class).setEnabled(true);
        get(Zoom.class).setEnabled(true);
        get(ItemSwap.class).setEnabled(true);
        get(Trajectories.class).setEnabled(true);
        get(TargetEsp.class).setEnabled(true);
        get(PotionsHUD.class).setEnabled(true);
    }

    private void register(Module module) {
        module.registerKeyBinding();
        modules.add(module);
    }

    public void tick() {
        if (MinecraftClient.getInstance().player == null) return;
        for (Module m : modules) {
            if (m.isBlocked()) continue;
            handleKey(m);
            if (m.isEnabled()) m.onTick();
        }
    }

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

    public void renderWorld(WorldRenderContext context) {
        Projection.capture(context);
        for (Module m : getVisibleModules()) {
            if (m.isEnabled()) m.onWorldRender(context);
        }
    }

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

    public void invalidateVisible() {
        visibleCache = null;
    }

    public List<Module> getModules() {
        return modules;
    }

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

    @SuppressWarnings("unchecked")
    public <T extends Module> T find(Class<T> type) {
        for (Module m : modules) {
            if (m.getClass() == type) return (T) m;
        }
        return null;
    }
}
