package wtf.gemini.modules.impl.movement;

import wtf.gemini.Module;

/** Ванильный ToggleSprint: бежим, пока зажато «вперёд». */
public class Sprint extends Module {
    public Sprint() {
        // featureId задан явно: сервер знает автопспринт как "sprint", а не как "togglesprint".
        super("ToggleSprint", Category.MOVEMENT, "Автоматический спринт при движении вперёд", "sprint");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.options.forwardKey.isPressed()
                && !mc.player.isSneaking()
                && !mc.player.horizontalCollision
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
