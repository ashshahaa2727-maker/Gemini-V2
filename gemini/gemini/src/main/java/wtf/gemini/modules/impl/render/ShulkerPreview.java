package wtf.gemini.modules.impl.render;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import wtf.gemini.Module;

/** В подсказке шалкера показывает сводку: сколько заполнено слотов и предметов. */
public class ShulkerPreview extends Module {
    public ShulkerPreview() {
        super("ShulkerPreview", Category.RENDER, "Сводка содержимого шалкера в подсказке");

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!isEnabled()) return;
            if (!(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)) return;

            ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
            if (container == null) return;

            int stacks = 0;
            long items = 0;
            for (ItemStack inner : container.iterateNonEmpty()) {
                stacks++;
                items += inner.getCount();
            }
            if (stacks == 0) return;

            lines.add(Text.literal("Gemini: " + stacks + "/27 слотов, " + items + " предметов")
                    .formatted(Formatting.AQUA));
        });
    }
}
