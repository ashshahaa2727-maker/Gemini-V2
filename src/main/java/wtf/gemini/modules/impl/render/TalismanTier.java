package wtf.gemini.modules.impl.render;

import java.util.Locale;

/** Уровни талисманов Лайт Анархии: Stinger -> Eternity -> Infinity (upgrade/create.md). */
public enum TalismanTier {
    STINGER(0xFF3B30, "stinger", "стингер"),
    ETERNITY(0xA855F7, "eternity", "этернити", "етернити"),
    INFINITY(0x34D058, "infinity", "инфинити");

    private static final String[] TALISMAN_WORDS = {"талисман", "talisman"};

    /** RGB без альфы. */
    public final int rgb;
    private final String[] keywords;

    TalismanTier(int rgb, String... keywords) {
        this.rgb = rgb;
        this.keywords = keywords;
    }

    /** Определяет уровень по названию предмета; null, если это не талисман нужного уровня. */
    public static TalismanTier fromName(String itemName) {
        if (itemName == null || itemName.isEmpty()) return null;
        String name = itemName.toLowerCase(Locale.ROOT);

        boolean talisman = false;
        for (String w : TALISMAN_WORDS) {
            if (name.contains(w)) { talisman = true; break; }
        }
        if (!talisman) return null; // «Меч Stinger», «Сфера Eternity» и т.п. не подсвечиваем

        for (TalismanTier tier : values()) {
            for (String k : tier.keywords) {
                if (name.contains(k)) return tier;
            }
        }
        return null;
    }
}
