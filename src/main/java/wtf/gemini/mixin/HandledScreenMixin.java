package wtf.gemini.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.gemini.Gemini;
import wtf.gemini.modules.impl.render.TalismanHighlight;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    /** Рисуем подсветку ПОД предметом: перед отрисовкой слота. */
    @Inject(method = "drawSlot", at = @At("HEAD"), require = 0)
    private void gemini$talismanBackground(DrawContext context, Slot slot, CallbackInfo ci) {
        if (Gemini.INSTANCE == null || Gemini.INSTANCE.moduleStorage == null || !slot.hasStack()) return;
        try {
            TalismanHighlight module = Gemini.INSTANCE.moduleStorage.find(TalismanHighlight.class);
            if (module != null && module.isEnabled()) {
                module.drawSlotBackground(context, slot.getStack(), slot.x, slot.y);
            }
        } catch (RuntimeException ignored) {
            // подсветка — косметика: любая ошибка не должна ронять отрисовку экрана
        }
    }
}
