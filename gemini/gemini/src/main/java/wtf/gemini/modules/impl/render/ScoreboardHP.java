package wtf.gemini.modules.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import wtf.gemini.Module;
import wtf.gemini.setting.BooleanSetting;

/** Показывает здоровье существа, на которое смотрит прицел. */
public class ScoreboardHP extends Module {
    public final BooleanSetting showDecimal = new BooleanSetting("Show Decimals", false);

    public ScoreboardHP() {
        super("ScoreboardHP", Category.RENDER, "Здоровье цели под прицелом");
        addSettings(showDecimal);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (mc.currentScreen != null) return;
        Entity target = mc.targetedEntity;
        if (!(target instanceof LivingEntity living) || living.isInvisible()) return;

        String text = showDecimal.get()
                ? String.format("\u2764 %.1f / %.1f", living.getHealth(), living.getMaxHealth())
                : String.format("\u2764 %d / %d", Math.round(living.getHealth()), Math.round(living.getMaxHealth()));

        int x = context.getScaledWindowWidth() / 2 - mc.textRenderer.getWidth(text) / 2;
        int y = context.getScaledWindowHeight() / 2 + 12;
        context.drawTextWithShadow(mc.textRenderer, text, x, y, 0xFFFF5555);
    }
}
