package wtf.gemini.modules.impl.player;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import wtf.gemini.Module;
import wtf.gemini.mixin.HandledScreenAccessor;
import wtf.gemini.setting.FloatSetting;

/** Shift + колесо мыши над слотом: быстро перемещает стак (как Shift+клик). */
public class ItemScroller extends Module {
    public final FloatSetting scrollDelay = new FloatSetting("Delay (ms)", 30f, 10f, 100f, 5f);
    private long lastMove = 0;

    public ItemScroller() {
        super("ItemScroller", Category.PLAYER, "Быстрое перемещение стаков колесиком мыши (Shift + скролл)");
        addSettings(scrollDelay);

        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof HandledScreen<?> && !(screen instanceof CreativeInventoryScreen)) {
                ScreenMouseEvents.allowMouseScroll(screen).register(
                        (s, mouseX, mouseY, horizontal, vertical) -> !handleScroll(s));
            }
        });
    }

    /** @return true, если скролл обработан (и ванильную обработку нужно отменить). */
    private boolean handleScroll(Screen screen) {
        if (!isEnabled() || !Screen.hasShiftDown()) return false;
        if (!(screen instanceof HandledScreen<?> hs)) return false;
        if (mc.player == null || mc.interactionManager == null) return false;

        Slot slot = ((HandledScreenAccessor) hs).gemini$getFocusedSlot();
        if (slot == null || !slot.hasStack()) return false;

        long now = System.currentTimeMillis();
        if (now - lastMove < scrollDelay.get()) return true;
        lastMove = now;

        mc.interactionManager.clickSlot(hs.getScreenHandler().syncId, slot.id, 0,
                SlotActionType.QUICK_MOVE, mc.player);
        return true;
    }
}
