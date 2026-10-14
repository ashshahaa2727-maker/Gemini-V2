package wtf.gemini.theme;

/**
 * Палитра клиента — единственный источник истины по цветам интерфейса.
 *
 * <p>Акцент задаёт игрок, остальные роли выводятся из акцента и режима (тёмная/светлая тема).
 * Модули и HUD не хранят собственные цвета: они спрашивают {@code Colors}, поэтому смена
 * акцента перекрашивает весь клиент сразу.
 */
public final class Colors {
    /** Подобранные акценты: 16 оттенков, а не случайные значения. */
    private static final int[] PRESETS = {
            0xFF38BDF8, // Sky
            0xFF60A5FA, // Blue
            0xFF6366F1, // Indigo
            0xFFA855F7, // Violet
            0xFFD946EF, // Fuchsia
            0xFFEC4899, // Rose
            0xFFF43F5E, // Crimson
            0xFFEF4444, // Red
            0xFFF97316, // Orange
            0xFFF59E0B, // Amber
            0xFFEAB308, // Gold
            0xFF84CC16, // Lime
            0xFF22C55E, // Emerald
            0xFF10B981, // Green
            0xFF14B8A6, // Teal
            0xFF06B6D4, // Cyan
    };

    private static final String[] PRESET_NAMES = {
            "Sky", "Blue", "Indigo", "Violet", "Fuchsia", "Rose", "Crimson", "Red",
            "Orange", "Amber", "Gold", "Lime", "Emerald", "Green", "Teal", "Cyan",
    };

    private static int accent = PRESETS[0];
    private static boolean dark = true;

    private Colors() {
    }

    public static int[] presets() {
        return PRESETS;
    }

    public static String[] presetNames() {
        return PRESET_NAMES;
    }

    public static int accent() {
        return accent;
    }

    public static void setAccent(int argb) {
        accent = argb | 0xFF000000;
    }

    /** Ставит акцент из палитры по индексу (с приведением к диапазону). */
    public static void setAccentPreset(int index) {
        if (PRESETS.length == 0) {
            return;
        }
        int i = Math.floorMod(index, PRESETS.length);
        setAccent(PRESETS[i]);
    }

    /** @return индекс акцента в палитре или -1, если выставлен свой цвет */
    public static int accentPresetIndex() {
        for (int i = 0; i < PRESETS.length; i++) {
            if (PRESETS[i] == accent) return i;
        }
        return -1;
    }

    public static boolean isDark() {
        return dark;
    }

    public static void setDark(boolean value) {
        dark = value;
    }

    // --- Роли палитры ---

    public static int background() {
        return dark ? 0xB0101018 : 0xE8F2F3F7;
    }

    public static int surface() {
        return dark ? 0x70161B24 : 0x80FFFFFF;
    }

    public static int text() {
        return dark ? 0xFFFFFFFF : 0xFF10141C;
    }

    public static int muted() {
        return dark ? 0xFF9CA3AF : 0xFF58606E;
    }

    /** Тёмный текст — для заливок акцентом и других светлых фонов. */
    public static int darkText() {
        return 0xFF0B1220;
    }

    public static int outline() {
        return withAlpha(accent, 0xAA);
    }

    /** Заливка включённого элемента: тот же акцент, но полупрозрачный. */
    public static int accentFill() {
        return withAlpha(accent, 0x55);
    }

    public static int accentSoft() {
        return withAlpha(accent, 0x22);
    }

    /** Цвет текста, читаемый на указанном фоне. */
    public static int readableOn(int backgroundColor) {
        return luminance(backgroundColor) > 0.55 ? darkText() : 0xFFFFFFFF;
    }

    // --- Утилиты ---

    public static int lerp(int from, int to, float t) {
        float k = Math.max(0f, Math.min(1f, t));
        int aa = (from >>> 24) & 255, ba = (to >>> 24) & 255;
        int ar = (from >> 16) & 255, br = (to >> 16) & 255;
        int ag = (from >> 8) & 255, bg = (to >> 8) & 255;
        int ab = from & 255, bb = to & 255;
        return (((int) (aa + (ba - aa) * k)) << 24)
                | (((int) (ar + (br - ar) * k)) << 16)
                | (((int) (ag + (bg - ag) * k)) << 8)
                | ((int) (ab + (bb - ab) * k));
    }

    public static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | ((alpha & 255) << 24);
    }

    public static int multiplyAlpha(int argb, float factor) {
        int alpha = (int) (((argb >>> 24) & 255) * Math.max(0f, Math.min(1f, factor)));
        return withAlpha(argb, alpha);
    }

    /** Относительная яркость по стандартным весам восприятия. */
    public static double luminance(int argb) {
        double r = ((argb >> 16) & 255) / 255.0;
        double g = ((argb >> 8) & 255) / 255.0;
        double b = (argb & 255) / 255.0;
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    /**
     * Цвет из HSB; hue — в градусах {@code 0..360}.
     *
     * <p>Считается вручную, а не через {@code java.awt.Color}: рантайм Minecraft не обязан
     * содержать модуль {@code java.desktop}, и зависеть от него в клиенте незачем.
     */
    public static int hsb(float hue, float saturation, float brightness) {
        float h = (((hue % 360f) + 360f) % 360f) / 60f;
        float s = Math.max(0f, Math.min(1f, saturation));
        float b = Math.max(0f, Math.min(1f, brightness));

        int sector = (int) Math.floor(h);
        float f = h - sector;
        float p = b * (1f - s);
        float q = b * (1f - s * f);
        float t = b * (1f - s * (1f - f));

        float r, g, bl;
        switch (Math.floorMod(sector, 6)) {
            case 0 -> { r = b; g = t; bl = p; }
            case 1 -> { r = q; g = b; bl = p; }
            case 2 -> { r = p; g = b; bl = t; }
            case 3 -> { r = p; g = q; bl = b; }
            case 4 -> { r = t; g = p; bl = b; }
            default -> { r = b; g = p; bl = q; }
        }
        return 0xFF000000 | (toByte(r) << 16) | (toByte(g) << 8) | toByte(bl);
    }

    private static int toByte(float channel) {
        return Math.max(0, Math.min(255, Math.round(channel * 255f)));
    }

    /** Сдвигает оттенок цвета — используется для производных оттенков палитры. */
    public static int shiftHue(int argb, float degrees) {
        float r = ((argb >> 16) & 255) / 255f;
        float g = ((argb >> 8) & 255) / 255f;
        float b = (argb & 255) / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue = 0f;
        if (delta > 0f) {
            if (max == r) {
                hue = 60f * (((g - b) / delta) % 6f);
            } else if (max == g) {
                hue = 60f * (((b - r) / delta) + 2f);
            } else {
                hue = 60f * (((r - g) / delta) + 4f);
            }
        }
        float saturation = max <= 0f ? 0f : delta / max;

        return withAlpha(hsb(hue + degrees, saturation, max), (argb >>> 24) & 255);
    }
}
