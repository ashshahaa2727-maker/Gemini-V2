package wtf.gemini.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.function.Predicate;

/**
 * Работа с инвентарём через {@code clickSlot} — ровно так же, как это делает ванильный клиент.
 *
 * <p>Пакеты вручную не собираются: interaction manager сам отправляет нужный пакет и
 * обновляет локальное состояние, поэтому рассинхрона с сервером не возникает.
 */
public final class InventoryUtil {
    /**
     * Кнопка {@code 40} в {@link SlotActionType#SWAP} — специальное значение «обменять
     * с левой рукой»; так же работает ванильная клавиша смены предмета в оффхенд.
     */
    private static final int OFFHAND_BUTTON = 40;

    private InventoryUtil() {
    }

    /**
     * Переводит индекс инвентаря игрока в индекс слота обработчика {@code PlayerScreenHandler}.
     *
     * <p>В инвентаре 0..8 — хотбар, 9..35 — основной. В обработчике слотов основной лежит
     * в 9..35, а хотбар — в 36..44.
     */
    public static int toScreenSlot(int inventoryIndex) {
        return inventoryIndex < 9 ? 36 + inventoryIndex : inventoryIndex;
    }

    /**
     * Меняет предмет из инвентаря с левой рукой.
     *
     * @param inventoryIndex индекс в инвентаре игрока (0..35)
     * @return true, если обмен отправлен
     */
    public static boolean swapWithOffhand(int inventoryIndex) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) {
            return false;
        }
        if (mc.player.getInventory().getStack(inventoryIndex).isEmpty()) {
            return false;
        }

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                toScreenSlot(inventoryIndex), OFFHAND_BUTTON, SlotActionType.SWAP, mc.player);
        return true;
    }

    /**
     * Ищет лучший слот в инвентаре по предикату.
     *
     * @param predicate какие предметы подходят
     * @param rank      чем больше, тем предпочтительнее предмет
     * @return индекс в инвентаре или {@code -1}
     */
    public static int findBest(Predicate<ItemStack> predicate, java.util.function.ToIntFunction<ItemStack> rank) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return -1;
        }
        int best = -1;
        int bestRank = Integer.MIN_VALUE;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !predicate.test(stack)) {
                continue;
            }
            int score = rank.applyAsInt(stack);
            if (score > bestRank) {
                bestRank = score;
                best = i;
            }
        }
        return best;
    }
}
