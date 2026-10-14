package wtf.gemini.hud;

import wtf.gemini.Module;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.setting.FloatSetting;
import wtf.gemini.util.Render2D;

/** Общий стиль панелей: свечение и радиус скругления (меню -> HudStyle -> ПКМ). */
public class HudStyle extends Module {
    public final BooleanSetting glow = new BooleanSetting("Glow", true);
    public final FloatSetting radius = new FloatSetting("Radius", 4f, 1f, 6f, 1f);

    public HudStyle() {
        super("HudStyle", Category.HUD, "Свечение и скругление панелей");
        addSettings(glow, radius);
    }

    @Override
    public void onTick() {
        Render2D.glow = glow.get();
        Render2D.radius = Math.round(radius.get());
    }

    @Override
    public void onDisable() {
        Render2D.glow = false;
        Render2D.radius = 3;
    }
}
