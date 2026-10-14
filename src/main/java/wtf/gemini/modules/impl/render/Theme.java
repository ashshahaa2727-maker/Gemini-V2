package wtf.gemini.modules.impl.render;

import wtf.gemini.Module;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.setting.ColorSetting;
import wtf.gemini.setting.ModeSetting;
import wtf.gemini.theme.Colors;

/**
 * Оформление клиента: акцент и тёмная/светлая тема.
 *
 * <p>Акцент берётся либо из палитры (16 подобранных оттенков), либо из своего цвета.
 * Остальные цвета интерфейса выводятся из акцента, поэтому настраивать каждый модуль
 * по отдельности не нужно — меняется весь клиент сразу.
 */
public class Theme extends Module {
    public final BooleanSetting usePalette = new BooleanSetting("Use Palette", true);
    public final ModeSetting palette = new ModeSetting("Palette", 0, Colors.presetNames());
    public final ColorSetting customAccent = new ColorSetting("Custom Accent", Colors.presets()[0]);
    public final BooleanSetting darkTheme = new BooleanSetting("Dark Theme", true);
    /** Выбор внешнего вида меню: панели по категориям, список с поиском или узкая колонка. */
    public final ModeSetting menuStyle = new ModeSetting("Menu Style", 0, "Panel", "List", "Minimal");

    public Theme() {
        super("Theme", Category.RENDER, "Палитра акцента и оформление интерфейса");
        addSettings(usePalette, palette, customAccent, darkTheme, menuStyle);
    }

    @Override
    public void onEnable() {
        apply();
    }

    @Override
    public void onTick() {
        apply();
    }

    private void apply() {
        Colors.setAccent(usePalette.get() ? Colors.presets()[palette.getIndex()] : customAccent.get());
        Colors.setDark(darkTheme.get());
    }
}
