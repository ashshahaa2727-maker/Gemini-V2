package wtf.gemini.modules.impl.render;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.ActionResult;
import wtf.gemini.Module;

/** Белый крестик у прицела при ударе по существу. */
public class HitMarker extends Module {
    private int ticks = 0;

    public HitMarker() {
        super("HitMarker", Category.RENDER, "Маркер попадания у прицела");

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && isEnabled()) ticks = 6;
            return ActionResult.PASS;
        });
    }

    @Override
    public void onTick() {
        if (ticks > 0) ticks--;
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (ticks <= 0) return;
        int cx = context.getScaledWindowWidth() / 2;
        int cy = context.getScaledWindowHeight() / 2;
        int color = 0xFFFFFFFF;
        for (int i = 3; i <= 7; i++) {
            context.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, color);
            context.fill(cx - i - 1, cy + i, cx - i, cy + i + 1, color);
            context.fill(cx + i, cy - i - 1, cx + i + 1, cy - i, color);
            context.fill(cx - i - 1, cy - i - 1, cx - i, cy - i, color);
        }
    }
}
