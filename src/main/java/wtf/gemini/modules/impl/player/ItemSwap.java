package wtf.gemini.modules.impl.player;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import wtf.gemini.Gemini;
import wtf.gemini.Module;
import wtf.gemini.holyworld.HolyWorldItems;
import wtf.gemini.setting.ModeSetting;
import wtf.gemini.util.InventoryUtil;

import java.util.function.Predicate;

public class ItemSwap extends Module {
    private static final String[] PREFER_KEYWORDS = {"any", "урон", "броня", "скорость"};

    public final ModeSetting prefer = new ModeSetting("Prefer Sphere", 0, "Any", "Damage", "Armor", "Speed");
    public final ModeSetting fallback = new ModeSetting("Fallback", 0, "Totem", "Shield", "None");

    public ItemSwap() {
        super("ItemSwap", Category.PLAYER, "Умная смена талисмана/сферы/тотема во второй руке");
        addSettings(prefer, fallback);
    }

    @Override
    public boolean isActionModule() {
        return true;
    }

    @Override
    public void activate() {
        if (mc.player == null) return;

        ItemStack offhand = mc.player.getOffHandStack();
        int slotToSwap = -1;

        // Если в руке Сфера или Пусто -> Берем Тотем/Талисман
        if (HolyWorldItems.isSphere(offhand) || offhand.isEmpty() || (!HolyWorldItems.isTalisman(offhand) && !HolyWorldItems.isSphere(offhand))) {
            slotToSwap = InventoryUtil.findBest(HolyWorldItems::isTalisman, HolyWorldItems::rank);
            
            // Фолбэк, если тотема/талисмана нет
            if (slotToSwap < 0) {
                if (fallback.get().equals("Shield")) {
                    slotToSwap = InventoryUtil.findBest(s -> s.isOf(Items.SHIELD), s -> 1);
                } else if (fallback.get().equals("Totem")) {
                    slotToSwap = InventoryUtil.findBest(s -> s.isOf(Items.TOTEM_OF_UNDYING), s -> 1);
                }
                
                // Если и щита нет, то хотя бы сферу возьмем
                if (slotToSwap < 0) {
                    slotToSwap = findSphere();
                }
            }
        } 
        // Если в руке Тотем/Талисман/Щит -> Берем Сферу
        else {
            slotToSwap = findSphere();
            // Если сферы нет, фолбэк на тотем
            if (slotToSwap < 0) {
                 slotToSwap = InventoryUtil.findBest(HolyWorldItems::isTalisman, HolyWorldItems::rank);
            }
        }

        if (slotToSwap >= 0) {
            InventoryUtil.swapWithOffhand(slotToSwap);
            Gemini.notifyPlayer("Swap: Предмет успешно изменен");
        } else {
            Gemini.notifyPlayer("Swap: Нечего брать в инвентаре!");
        }
    }

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
