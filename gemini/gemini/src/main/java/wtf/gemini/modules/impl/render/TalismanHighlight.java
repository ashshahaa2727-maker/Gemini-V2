package wtf.gemini.modules.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import wtf.gemini.Module;
import wtf.gemini.setting.BooleanSetting;

/**
 * Подсветка талисманов: Stinger — красный, Eternity — фиолетовый, Infinity — зелёный.
 * Рисуется только фоном слота в инвентаре/контейнерах и рамкой в хотбаре и второй руке.
 * Предметы в мире не подсвечиваются.
 */
public class TalismanHighlight extends Module {
    public final BooleanSetting hotbar = new BooleanSetting("Hotbar & Offhand", true);

    private static final int FILL_ALPHA = 0x90 << 24;
    private static final int BORDER_ALPHA = 0xFF << 24;

    public TalismanHighlight() {
        super("TalismanHighlight", Category.RENDER, "Подсветка талисманов Stinger / Eternity / Infinity");
        addSettings(hotbar);
    }

    /** @return уровень талисмана или null. Безопасен для вызова из миксина. */
    public static TalismanTier tierOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        return TalismanTier.fromName(stack.getName().getString());
    }

    /** Фон слота в GUI (вызывается миксином перед отрисовкой предмета). Координаты — как у Slot.x / Slot.y. */
    public void drawSlotBackground(DrawContext context, ItemStack stack, int x, int y) {
        TalismanTier tier = tierOf(stack);
        if (tier == null) return;
        context.fill(x, y, x + 16, y + 16, FILL_ALPHA | tier.rgb);
        frame(context, x - 1, y - 1, 18, BORDER_ALPHA | tier.rgb);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (!hotbar.get() || mc.player == null) return;

        int cx = context.getScaledWindowWidth() / 2;
        int itemY = context.getScaledWindowHeight() - 16 - 3;

        for (int i = 0; i < 9; i++) {
            mark(context, mc.player.getInventory().getStack(i), cx - 90 + i * 20 + 2, itemY);
        }

        boolean rightHanded = mc.options.getMainArm().getValue() == Arm.RIGHT;
        int offhandX = rightHanded ? cx - 91 - 26 : cx + 91 + 10;
        mark(context, mc.player.getOffHandStack(), offhandX, itemY);
    }

    /** В HUD предмет уже нарисован, поэтому только рамка (не закрывает иконку). */
    private void mark(DrawContext context, ItemStack stack, int x, int y) {
        TalismanTier tier = tierOf(stack);
        if (tier == null) return;
        frame(context, x - 1, y - 1, 18, BORDER_ALPHA | tier.rgb);
    }

    private static void frame(DrawContext context, int x, int y, int size, int argb) {
        context.fill(x, y, x + size, y + 1, argb);               // верх
        context.fill(x, y + size - 1, x + size, y + size, argb); // низ
        context.fill(x, y, x + 1, y + size, argb);               // лево
        context.fill(x + size - 1, y, x + size, y + size, argb); // право
    }
}
