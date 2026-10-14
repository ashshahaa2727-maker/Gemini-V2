package wtf.gemini.util;

import net.minecraft.client.gui.DrawContext;
import wtf.gemini.theme.Colors;

/**
 * Лёгкие 2D-примитивы для HUD: без аллокаций и без GLSL. Таблицы скругления считаются один раз.
 *
 * <p>Цвета здесь не хранятся: примитивы спрашивают их у {@link Colors}, поэтому весь
 * интерфейс автоматически следует за темой, а смена акцента перекрашивает его целиком.
 */
public final class Render2D {
    /** Настраиваются модулем HudStyle. */
    public static boolean glow = true;
    public static int radius = 4;

    private static final int MAX_R = 8;
    private static final int[][] IX = new int[MAX_R + 1][];
    private static final float[][] COV = new float[MAX_R + 1][];

    static {
        for (int r = 1; r <= MAX_R; r++) {
            IX[r] = new int[r];
            COV[r] = new float[r];
            for (int i = 0; i < r; i++) {
                double dy = r - i - 0.5;
                double xc = r - Math.sqrt(r * r - dy * dy); // горизонтальный отступ границы дуги
                int ix = (int) Math.floor(xc);
                IX[r][i] = ix;
                COV[r][i] = (float) (1.0 - (xc - ix));      // покрытие крайнего пикселя
            }
        }
    }

    private Render2D() {}

    /** Скруглённый прямоугольник со сглаженными краями (крайние пиксели с частичной прозрачностью). */
    public static void roundedRect(DrawContext c, int x, int y, int w, int h, int rad, int argb) {
        int r = Math.max(0, Math.min(rad, Math.min(MAX_R, Math.min(w, h) / 2)));
        if (r == 0) {
            c.fill(x, y, x + w, y + h, argb);
            return;
        }
        int a = (argb >>> 24) & 255;
        int rgb = argb & 0xFFFFFF;
        int[] ix = IX[r];
        float[] cov = COV[r];
        for (int i = 0; i < r; i++) {
            int edge = ((int) (a * cov[i]) << 24) | rgb;
            int top = y + i, bottom = y + h - 1 - i;
            int l = x + ix[i], rr = x + w - ix[i];
            c.fill(l + 1, top, rr - 1, top + 1, argb);
            c.fill(l, top, l + 1, top + 1, edge);
            c.fill(rr - 1, top, rr, top + 1, edge);
            c.fill(l + 1, bottom, rr - 1, bottom + 1, argb);
            c.fill(l, bottom, l + 1, bottom + 1, edge);
            c.fill(rr - 1, bottom, rr, bottom + 1, edge);
        }
        c.fill(x, y + r, x + w, y + h - r, argb);
    }

    /** Мягкое свечение: три расширяющихся слоя с убывающей прозрачностью. */
    public static void glowRect(DrawContext c, int x, int y, int w, int h, int rad, int rgb) {
        roundedRect(c, x - 3, y - 3, w + 6, h + 6, rad + 3, (0x0E << 24) | (rgb & 0xFFFFFF));
        roundedRect(c, x - 2, y - 2, w + 4, h + 4, rad + 2, (0x1A << 24) | (rgb & 0xFFFFFF));
        roundedRect(c, x - 1, y - 1, w + 2, h + 2, rad + 1, (0x30 << 24) | (rgb & 0xFFFFFF));
    }

    /** Стандартная панель клиента: свечение (если включено) + фон + акцентная линия снизу. */
    public static void panel(DrawContext c, int x, int y, int w, int h) {
        int accent = Colors.accent();
        if (glow) glowRect(c, x, y, w, h, radius, accent);
        roundedRect(c, x, y, w, h, radius, Colors.background());
        c.fill(x + radius, y + h - 1, x + w - radius, y + h, accent);
    }

    public static void outline(DrawContext c, int x, int y, int w, int h, int argb) {
        c.fill(x, y, x + w, y + 1, argb);
        c.fill(x, y + h - 1, x + w, y + h, argb);
        c.fill(x, y, x + 1, y + h, argb);
        c.fill(x + w - 1, y, x + w, y + h, argb);
    }
}
