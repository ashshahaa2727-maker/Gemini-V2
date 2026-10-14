package wtf.gemini.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.gemini.Gemini;
import wtf.gemini.modules.impl.render.TotemPopSound;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onEntityStatus", at = @At("TAIL"))
    private void gemini$onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        // статус 35 = тотем бессмертия сработал
        if (packet.getStatus() != 35 || Gemini.INSTANCE == null || Gemini.INSTANCE.moduleStorage == null) {
            return;
        }
        // Пакет может прийти до того, как модули зарегистрированы, — тогда find вернёт null.
        TotemPopSound module = Gemini.INSTANCE.moduleStorage.find(TotemPopSound.class);
        if (module != null && module.isEnabled()) {
            module.play();
        }
    }
}
