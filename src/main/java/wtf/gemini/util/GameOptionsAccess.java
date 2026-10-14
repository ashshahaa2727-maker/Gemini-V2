package wtf.gemini.util;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import wtf.gemini.Gemini;

import java.lang.reflect.Field;

/**
 * Доступ к {@code GameOptions.allKeys}.
 *
 * <p>В ванилле поле объявлено {@code final}; Fabric API снимает final в рантайме
 * (миксин {@code GameOptionsMixin} в fabric-key-binding-api-v1 с {@code @Mutable}).
 * Своя миксина здесь не нужна — так мы не зависим от порядка применения миксинов.
 */
public final class GameOptionsAccess {
    private static Field allKeysField;
    private static boolean unavailable;

    private GameOptionsAccess() {
    }

    /**
     * Заменяет массив клавиш. Клавиши, которых нет в массиве, не обновляются игрой вообще,
     * поэтому {@code wasPressed()} для них всегда false — ровно то, что нужно скрытой функции.
     *
     * @return true, если массив удалось заменить
     */
    public static boolean setAllKeys(GameOptions options, KeyBinding[] keys) {
        if (unavailable || options == null) {
            return false;
        }
        try {
            Field field = allKeysField;
            if (field == null) {
                field = GameOptions.class.getDeclaredField("allKeys");
                field.setAccessible(true);
                allKeysField = field;
            }
            field.set(options, keys);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Скрытие клавиш — косметика; без него клиент работает, просто список будет полным.
            unavailable = true;
            Gemini.LOGGER.warn("[Gemini] Не удалось обновить список клавиш: {}", e.toString());
            return false;
        }
    }
}
