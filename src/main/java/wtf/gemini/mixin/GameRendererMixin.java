package wtf.gemini.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.gemini.modules.impl.render.Zoom;

/**
 * Приближение камеры: уменьшает FOV, который считает {@code GameRenderer#getFov}.
 *
 * <p>Метод в ванильном коде приватный и возвращает {@code float} — сигнатура сверена по
 * ремапнутому jar, поэтому дескриптор можно не указывать.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void gemini$applyZoom(Camera camera, float tickDelta, boolean changingFov,
                                  CallbackInfoReturnable<Float> cir) {
        float zoom = Zoom.multiplier();
        if (zoom > 1.0f) {
            cir.setReturnValue(cir.getReturnValueF() / zoom);
        }
    }
}
