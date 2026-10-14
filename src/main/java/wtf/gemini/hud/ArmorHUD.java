package wtf.gemini.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

/** Броня с процентом прочности; меньше 15% подсвечивается красным. */
public class ArmorHUD extends HudModule {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final int ROW = 18, W = 52;

    private final String[] pct = {"", "", "", ""};
    private final int[] color = new int[4];
    private final ItemStack[] stacks = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    public ArmorHUD() {
        super("ArmorHUD", Category.HUD, "Прочность брони");
    }

    @Override public int getWidth() { return W; }
    @Override public int getHeight() { return ROW * 4 + 4; }
    @Override protected int defaultX(int sw, int sh) { return sw - W - 6; }
    @Override protected int defaultY(int sw, int sh) { return sh - getHeight() - 6; }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        for (int i = 0; i < 4; i++) {
            ItemStack s = mc.player.getEquippedStack(SLOTS[i]);
            stacks[i] = s;
            if (s.isEmpty() || !s.isDamageable()) {
                pct[i] = "";
                continue;
            }
            int p = 100 - s.getDamage() * 100 / Math.max(1, s.getMaxDamage());
            pct[i] = p + "%";
            color[i] = p < 15 ? 0xFFEF4444 : p < 40 ? 0xFFFACC15 : Colors.text();
        }
    }

    @Override
    protected void renderAt(DrawContext c, int x, int y) {
        Render2D.panel(c, x, y, W, getHeight());
        for (int i = 0; i < 4; i++) {
            if (stacks[i].isEmpty()) continue;
            int ry = y + 2 + i * ROW;
            c.drawItem(stacks[i], x + 4, ry);
            if (!pct[i].isEmpty()) {
                c.drawTextWithShadow(mc.textRenderer, pct[i], x + 24, ry + 4, color[i]);
            }
        }
    }
}
