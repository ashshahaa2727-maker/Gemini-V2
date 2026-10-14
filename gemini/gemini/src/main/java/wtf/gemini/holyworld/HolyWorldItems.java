package wtf.gemini.holyworld;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Locale;

/**
 * Опознание предметов HolyWorld по отображаемому имени.
 *
 * <p>Сервер выдаёт кастомные предметы как переименованные ванильные, поэтому надёжнее всего
 * ориентироваться на имя, которое приходит в пакете: «Сфера Eternity», «Талисман Stinger».
 *
 * <p>Талисман дополнительно опознаётся как тотем бессмертия: по механикам сервера
 * объединение тотема со сферой сохраняет свойства тотема.
 */
public final class HolyWorldItems {
    private static final String[] SPHERE = {"сфера", "sphere"};
    private static final String[] TALISMAN = {"талисман", "talisman"};

    /**
     * Приоритет сфер: чем раньше встречается в списке, тем ценнее.
     * Уникальные сферы сервера идут выше легендарных.
     */
    private static final String[] RANK = {
            "инфинити", "infinity",
            "этернити", "eternity",
            "цербера", "cerber",
            "армоталити", "armortality",
            "имморталити", "immortality",
            "сатиры", "satyr",
            "флеша", "flash",
            "стингер", "stinger",
            "мифическ", "mythic",
            "легендарн", "legendary",
            "эпическ", "epic",
    };

    private HolyWorldItems() {
    }

    public static boolean isTalisman(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.isOf(Items.TOTEM_OF_UNDYING) || matches(stack, TALISMAN);
    }

    public static boolean isSphere(ItemStack stack) {
        return matches(stack, SPHERE);
    }

    /** Чем больше значение, тем ценнее сфера; неизвестные получают 0. */
    public static int rank(ItemStack stack) {
        String name = lowerName(stack);
        if (name.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < RANK.length; i++) {
            if (name.contains(RANK[i])) {
                return RANK.length - i;
            }
        }
        return 0;
    }

    /** Совпадает ли имя с одной из тематических подстрок. */
    public static boolean nameContains(ItemStack stack, String keyword) {
        return lowerName(stack).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private static boolean matches(ItemStack stack, String[] keywords) {
        String name = lowerName(stack);
        if (name.isEmpty()) {
            return false;
        }
        for (String keyword : keywords) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String lowerName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        return stack.getName().getString().toLowerCase(Locale.ROOT);
    }
}
