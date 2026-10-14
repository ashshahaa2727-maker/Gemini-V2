package wtf.gemini.modules.impl.render;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import wtf.gemini.Module;

/** Яркость в пещерах: клиентский эффект ночного зрения (без изменения гаммы). */
public class FullBright extends Module {
    public FullBright() {
        super("FullBright", Category.RENDER, "Яркость в пещерах");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 1_000_000, 0, false, false, false));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }
}
