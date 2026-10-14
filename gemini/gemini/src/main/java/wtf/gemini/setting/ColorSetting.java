package wtf.gemini.setting;

/**
 * Настройка цвета.
 *
 * <p>Хранит ARGB и сохраняется в конфиг строкой {@code #AARRGGBB} — так файл остаётся
 * читаемым руками, в отличие от «упакованного» в целое числа.
 */
public class ColorSetting extends Setting {
    private final boolean allowAlpha;
    private int value;

    public ColorSetting(String name, int value) {
        this(name, value, true);
    }

    public ColorSetting(String name, int value, boolean allowAlpha) {
        super(name);
        this.allowAlpha = allowAlpha;
        set(value);
    }

    public int get() {
        return value;
    }

    public void set(int argb) {
        this.value = allowAlpha ? argb : (argb | 0xFF000000);
    }

    public boolean allowsAlpha() {
        return allowAlpha;
    }

    public String toHex() {
        return String.format("#%08X", value);
    }

    /**
     * Разбирает {@code #RRGGBB} или {@code #AARRGGBB} (решётка необязательна).
     *
     * @return true, если строка разобрана и значение применено
     */
    public boolean fromHex(String hex) {
        if (hex == null) {
            return false;
        }
        String cleaned = hex.trim();
        if (cleaned.startsWith("#")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.length() == 6) {
            cleaned = "FF" + cleaned;
        }
        if (cleaned.length() != 8) {
            return false;
        }
        try {
            set((int) Long.parseLong(cleaned, 16));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
