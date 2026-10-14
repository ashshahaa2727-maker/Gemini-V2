package wtf.gemini.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.gemini.modules.impl.render.SeeInvisible;

/**
 * Возвращает невидимых существ в отрисовку.
 *
 * <p>В ванильном коде модель невидимки просто не рисуется: {@code isVisible} возвращает
 * {@code false}, из-за чего {@code getRenderLayer} отдаёт {@code null}. Одной прозрачности
 * мало — сначала нужно снова разрешить отрисовку, а затем задать видимую альфу.
 *
 * <p>Обе инъекции необязательные ({@code require = 0}): если в будущей версии игры сигнатуры
 * изменятся, клиент запустится, просто без этой функции.
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "isVisible", at = @At("RETURN"), cancellable = true, require = 0)
    private void gemini$revealInvisible(LivingEntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || !state.invisible || !SeeInvisible.isRevealing()) {
            return;
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "getMixColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void gemini$revealColor(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (!state.invisible || !SeeInvisible.isRevealing()) {
            return;
        }
        cir.setReturnValue(SeeInvisible.tint(cir.getReturnValueI()));
    }
}
