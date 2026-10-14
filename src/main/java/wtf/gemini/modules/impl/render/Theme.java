package wtf.gemini.modules.impl.render;

import wtf.gemini.Module;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.setting.ColorSetting;
import wtf.gemini.setting.FloatSetting;
import wtf.gemini.setting.ModeSetting;
import wtf.gemini.theme.Colors;

public class Theme extends Module {
    public final BooleanSetting usePalette = new BooleanSetting("Use Preset", true);
    public final ModeSetting palette = new ModeSetting("Preset", 0, Colors.presetNames());
    public final ColorSetting customAccent = new ColorSetting("Custom Accent", Colors.presets()[0]);

    // --- ПОЛНАЯ НАСТРОЙКА МЕНЮ ---
    public final FloatSetting rounding = new FloatSetting("Rounding", 4f, 0f, 15f, 1f);
    public final BooleanSetting showOutline = new BooleanSetting("Show Outline", true);
    public final ColorSetting outlineColor = new ColorSetting("Outline Color", 0x88000000);
    public final ColorSetting panelColor = new ColorSetting("Panel BG", 0xDD111111);
    public final ColorSetting elementColor = new ColorSetting("Element BG", 0x44FFFFFF);
    public final ColorSetting hoverColor = new ColorSetting("Hover BG", 0x66FFFFFF);
    public final ColorSetting moduleBgColor = new ColorSetting("Module BG", 0x55000000);
    public final ModeSetting menuStyle = new ModeSetting("Menu Style", 0, "Panel", "List", "Minimal");

    public Theme() {
        super("Theme", Category.RENDER, "Полная кастомизация меню и цветов клиента");
        addSettings(usePalette, palette, customAccent, rounding, showOutline, outlineColor, panelColor, elementColor, hoverColor, moduleBgColor, menuStyle);
    }

    @Override
    public void onEnable() { apply(); }

    @Override
    public void onTick() { apply(); }

    private void apply() {
        Colors.setAccent(usePalette.get() ? Colors.presets()[palette.getIndex()] : customAccent.get());
    }
}
