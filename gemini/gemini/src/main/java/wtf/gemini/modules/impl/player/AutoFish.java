package wtf.gemini.modules.impl.player;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import wtf.gemini.Module;
import wtf.gemini.mixin.FishingBobberEntityAccessor;
import wtf.gemini.setting.BooleanSetting;

/** Автоматически вытаскивает удочку при поклёвке и (опционально) забрасывает заново. */
public class AutoFish extends Module {
    public final BooleanSetting recast = new BooleanSetting("Auto Recast", true);

    private int reelDelay = 0;
    private int recastDelay = -1;

    public AutoFish() {
        super("AutoFish", Category.PLAYER, "Автоматическая рыбалка");
        addSettings(recast);
    }

    @Override
    public void onEnable() {
        reelDelay = 0;
        recastDelay = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.player.getMainHandStack().isOf(Items.FISHING_ROD)) {
            recastDelay = -1;
            return;
        }

        FishingBobberEntity bobber = mc.player.fishHook;
        if (bobber != null) {
            if (((FishingBobberEntityAccessor) bobber).gemini$hasCaughtFish()) {
                if (++reelDelay > 3) {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    reelDelay = 0;
                    recastDelay = recast.get() ? 20 : -1;
                }
            } else {
                reelDelay = 0;
            }
        } else if (recastDelay >= 0 && --recastDelay < 0) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
