package wtf.gemini.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

/** WASD со скруглёнными клавишами и плавной подсветкой нажатия. */
public class KeyStrokes extends HudModule {
    private static final int SIZE = 20, GAP = 2;

    private final float[] anim = new float[4]; // W, A, S, D

    public KeyStrokes() {
        super("KeyStrokes", Category.HUD, "Показ нажатых клавиш W A S D");
    }

    @Override public int getWidth() { return SIZE * 3 + GAP * 2; }
    @Override public int getHeight() { return SIZE * 2 + GAP; }
    @Override protected int defaultX(int sw, int sh) { return 6; }
    @Override protected int defaultY(int sw, int sh) { return sh - getHeight() - 6; }

    @Override
    public void onTick() {
        step(0, mc.options.forwardKey);
        step(1, mc.options.leftKey);
        step(2, mc.options.backKey);
        step(3, mc.options.rightKey);
    }

    private void step(int i, KeyBinding key) {
        float target = key.isPressed() ? 1f : 0f;
        anim[i] += (target - anim[i]) * 0.4f;
        if (Math.abs(target - anim[i]) < 0.01f) anim[i] = target;
    }

    @Override
    protected void renderAt(DrawContext context, int x, int y) {
        key(context, "W", anim[0], x + SIZE + GAP, y);
        key(context, "A", anim[1], x, y + SIZE + GAP);
        key(context, "S", anim[2], x + SIZE + GAP, y + SIZE + GAP);
        key(context, "D", anim[3], x + (SIZE + GAP) * 2, y + SIZE + GAP);
    }

    private void key(DrawContext context, String label, float t, int x, int y) {
        int pressed = Colors.withAlpha(Colors.accent(), 0xE0);
        Render2D.roundedRect(context, x, y, SIZE, SIZE, Math.min(Render2D.radius, 4),
                Colors.lerp(Colors.background(), pressed, t));
        int textColor = Colors.lerp(Colors.text(), Colors.darkText(), t);
        context.drawCenteredTextWithShadow(mc.textRenderer, label, x + SIZE / 2, y + 6, textColor);
    }
}
