package wtf.gemini.modules.impl.player;

import net.minecraft.item.ItemStack;
import wtf.gemini.Gemini;
import wtf.gemini.Module;
import wtf.gemini.holyworld.HolyWorldItems;
import wtf.gemini.setting.ModeSetting;
import wtf.gemini.util.InventoryUtil;

import java.util.function.Predicate;

/**
 * Смена талисмана и сферы в левой руке одной клавишей.
 *
 * <p>По механикам HolyWorld эффекты сфер и талисманов работают <b>только во второй руке</b>,
 * поэтому переключение между ними — частое действие в бою.
 *
 * <p>Модуль — «действие»: он включён и ждёт нажатия клавиши, состояние при этом не меняется.
 */
public class ItemSwap extends Module {
    private static final String[] PREFER_KEYWORDS = {"any", "урон", "броня", "скорость"};

    public final ModeSetting prefer = new ModeSetting("Prefer", 0, "Any", "Damage", "Armor", "Speed");

    public ItemSwap() {
        super("ItemSwap", Category.PLAYER, "Смена талисмана и сферы во второй руке по клавише");
        addSettings(prefer);
    }

    /** Действие выполняется по нажатию, а не переключает модуль. */
    @Override
    public boolean isActionModule() {
        return true;
    }

    @Override
    public void activate() {
        if (mc.player == null) {
            return;
        }

        ItemStack offhand = mc.player.getOffHandStack();
        int slot;
        if (HolyWorldItems.isTalisman(offhand)) {
            slot = findSphere();
        } else if (HolyWorldItems.isSphere(offhand)) {
            slot = InventoryUtil.findBest(HolyWorldItems::isTalisman, HolyWorldItems::rank);
        } else {
            // В руке нет ни того ни другого — выкладываем лучшую сферу.
            slot = findSphere();
        }

        if (slot < 0 || !InventoryUtil.swapWithOffhand(slot)) {
            Gemini.notifyPlayer("ItemSwap: нечего менять");
            return;
        }
        Gemini.notifyPlayer("ItemSwap: смена в левой руке");
    }

    /**
     * Ищет сферу по выбранному приоритету.
     *
     * <p>Если сфер нужного типа нет — берётся лучшая из остальных: пустая левая рука
     * в бою хуже, чем сфера «не того» эффекта.
     */
    private int findSphere() {
        int index = prefer.getIndex();
        if (index > 0 && index < PREFER_KEYWORDS.length) {
            String keyword = PREFER_KEYWORDS[index];
            Predicate<ItemStack> typed = stack ->
                    HolyWorldItems.isSphere(stack) && HolyWorldItems.nameContains(stack, keyword);
            int typedSlot = InventoryUtil.findBest(typed, HolyWorldItems::rank);
            if (typedSlot >= 0) {
                return typedSlot;
            }
        }
        return InventoryUtil.findBest(HolyWorldItems::isSphere, HolyWorldItems::rank);
    }
}
