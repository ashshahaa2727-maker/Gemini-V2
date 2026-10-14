package wtf.gemini.ui.menu;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import wtf.gemini.Gemini;
import wtf.gemini.Module;
import wtf.gemini.modules.impl.render.Theme;
import wtf.gemini.setting.*;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

public final class MenuDraw {
    private MenuDraw() {}

    public static void moduleRow(DrawContext ctx, TextRenderer text, int x, int y, int w, int h,
                                 Module module, boolean expanded, boolean hover) {
        Theme theme = Gemini.INSTANCE.moduleStorage.get(Theme.class);
        int rad = Math.round(theme.rounding.get());
        int modBg = theme.moduleBgColor.get();
        
        // Цвет модуля (светлеет при наведении)
        int bgColor = hover ? Colors.lerp(modBg, 0xFFFFFF, 0.15f) : modBg;
        Render2D.roundedRect(ctx, x, y, w, h - 1, rad, bgColor);

        // Заливка цветом акцента, если включен
        if (module.isEnabled()) {
            Render2D.roundedRect(ctx, x, y, w, h - 1, rad, Colors.withAlpha(Colors.accent(), 0x77));
        }

        // Обводка
        if (theme.showOutline.get()) {
            Render2D.outline(ctx, x, y, w, h - 1, module.isEnabled() ? Colors.accent() : theme.outlineColor.get());
        }

        int textColor = module.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA;
        ctx.drawTextWithShadow(text, module.getName(), x + 4, y + 4, textColor);

        if (!module.getSettings().isEmpty()) {
            ctx.drawTextWithShadow(text, expanded ? "-" : "+", x + w - 10, y + 4, 0xFFFFFFFF);
        }
    }

    public static void settingRow(DrawContext ctx, TextRenderer text, int x, int y, int w, int h,
                                  Setting setting, boolean hover) {
        Theme theme = Gemini.INSTANCE.moduleStorage.get(Theme.class);
        int rad = Math.round(theme.rounding.get());

        if (hover) {
            Render2D.roundedRect(ctx, x, y, w, h, rad, theme.hoverColor.get());
        } else {
            Render2D.roundedRect(ctx, x, y, w, h - 1, rad, theme.elementColor.get());
        }

        if (setting instanceof BooleanSetting bool) {
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 4, 0xFFDDDDDD);
            Render2D.roundedRect(ctx, x + w - 14, y + 3, 9, 9, rad, bool.get() ? Colors.accent() : theme.moduleBgColor.get());
        } else if (setting instanceof FloatSetting number) {
            String value = number.getStep() >= 1f ? Integer.toString(Math.round(number.get()))
                    : Float.toString(Math.round(number.get() * 10f) / 10f);
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 1, 0xFFDDDDDD);
            ctx.drawTextWithShadow(text, value, x + w - 4 - text.getWidth(value), y + 1, 0xFFFFFFFF);

            int bx = x + 6, bw = w - 12, by = y + 11;
            Render2D.roundedRect(ctx, bx, by, bw, 2, 1, theme.moduleBgColor.get());
            float ratio = (number.get() - number.getMin()) / (number.getMax() - number.getMin());
            if (ratio > 0) {
                Render2D.roundedRect(ctx, bx, by, Math.round(bw * ratio), 2, 1, Colors.accent());
            }
        } else if (setting instanceof ModeSetting mode) {
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 4, 0xFFDDDDDD);
            String value = mode.get();
            ctx.drawTextWithShadow(text, value, x + w - 4 - text.getWidth(value), y + 4, Colors.accent());
        } else if (setting instanceof ColorSetting color) {
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 4, 0xFFDDDDDD);
            Render2D.roundedRect(ctx, x + w - 14, y + 2, 11, 11, rad, 0xFFFFFFFF);
            Render2D.roundedRect(ctx, x + w - 13, y + 3, 9, 9, rad, color.get());
        } else {
            ctx.drawTextWithShadow(text, setting.getName(), x + 6, y + 4, 0xFFDDDDDD);
        }
    }

    public static void header(DrawContext ctx, TextRenderer text, int x, int y, int w, String title) {
        ctx.drawCenteredTextWithShadow(text, title, x + w / 2, y, Colors.accent());
    }
}
