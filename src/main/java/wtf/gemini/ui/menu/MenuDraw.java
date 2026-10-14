package wtf.gemini.ui.menu;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import wtf.gemini.Module;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.setting.ColorSetting;
import wtf.gemini.setting.FloatSetting;
import wtf.gemini.setting.ModeSetting;
import wtf.gemini.setting.Setting;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

/**
 * Отрисовка строк меню.
 *
 * <p>Вынесена отдельно, потому что все стили рисуют строки одинаково — отличаются только
 * расположением. Так внешний вид настройки правится в одном месте.
 */
public final class MenuDraw {
    private MenuDraw() {
    }

    /** Строка модуля: подсветка включённого, название и признак раскрытых настроек. */
    public static void moduleRow(DrawContext ctx, TextRenderer text, int x, int y, int w, int h,
                                 Module module, boolean expanded, boolean hover) {
        if (module.isEnabled()) {
            Render2D.roundedRect(ctx, x, y, w, h - 1, 3, Colors.accentFill());
        } else if (hover) {
            Render2D.roundedRect(ctx, x, y, w, h - 1, 3, Colors.withAlpha(0xFFFFFF, 0x22));
        }

        ctx.drawTextWithShadow(text, module.getName(), x + 4, y + 4,
                module.isEnabled() ? Colors.text() : Colors.muted());

        if (!module.getSettings().isEmpty()) {
            ctx.drawTextWithShadow(text, expanded ? "-" : "+", x + w - 10, y + 4, Colors.muted());
        }
    }

    /** Строка настройки: вид зависит от её типа. */
    public static void settingRow(DrawContext ctx, TextRenderer text, int x, int y, int w, int h,
                                  Setting setting, boolean hover) {
        if (hover) {
            Render2D.roundedRect(ctx, x, y, w, h, 2, Colors.withAlpha(0xFFFFFF, 0x18));
        }

        if (setting instanceof BooleanSetting bool) {
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 4, Colors.muted());
            Render2D.roundedRect(ctx, x + w - 14, y + 3, 9, 9, 2,
                    bool.get() ? Colors.accent() : 0x66000000);
        } else if (setting instanceof FloatSetting number) {
            String value = number.getStep() >= 1f ? Integer.toString(Math.round(number.get()))
                    : Float.toString(Math.round(number.get() * 10f) / 10f);
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 1, Colors.muted());
            ctx.drawTextWithShadow(text, value, x + w - 4 - text.getWidth(value), y + 1, Colors.text());

            int bx = x + 6, bw = w - 12, by = y + 11;
            ctx.fill(bx, by, bx + bw, by + 2, 0x55FFFFFF);
            float ratio = (number.get() - number.getMin()) / (number.getMax() - number.getMin());
            ctx.fill(bx, by, bx + Math.round(bw * ratio), by + 2, Colors.accent());
        } else if (setting instanceof ModeSetting mode) {
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 4, Colors.muted());
            String value = mode.get();
            ctx.drawTextWithShadow(text, value, x + w - 4 - text.getWidth(value), y + 4, Colors.accent());
        } else if (setting instanceof ColorSetting color) {
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 4, Colors.muted());
            Render2D.roundedRect(ctx, x + w - 13, y + 3, 9, 9, 2, color.get());
        } else {
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 4, Colors.muted());
        }
    }

    /** Заголовок раздела. */
    public static void header(DrawContext ctx, TextRenderer text, int x, int y, int w, String title) {
        ctx.drawCenteredTextWithShadow(text, title, x + w / 2, y, Colors.accent());
    }
}
