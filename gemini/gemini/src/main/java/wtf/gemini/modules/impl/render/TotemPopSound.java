package wtf.gemini.modules.impl.render;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import wtf.gemini.Module;

/** Дополнительный звук, когда у кого-то рядом срабатывает тотем бессмертия. */
public class TotemPopSound extends Module {
    public TotemPopSound() {
        super("TotemPopSound", Category.RENDER, "Звук при срабатывании тотема");
    }

    /** Вызывается из ClientPlayNetworkHandlerMixin. */
    public void play() {
        mc.getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.0f));
    }
}
