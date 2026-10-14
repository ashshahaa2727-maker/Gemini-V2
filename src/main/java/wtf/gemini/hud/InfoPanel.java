package wtf.gemini.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

/** Координаты, сторона света и скорость (блоков/сек). Строки пересобираются 5 раз в секунду. */
public class InfoPanel extends HudModule {
    private String line1 = "XYZ  0  0  0";
    private String line2 = "N  |  0.0 b/s";
    private int width = 70;
    private Vec3d lastPos;
    private float speed = 0f;

    public InfoPanel() {
        super("InfoPanel", Category.HUD, "Координаты, направление и скорость");
    }

    @Override public int getWidth() { return width + 14; }
    @Override public int getHeight() { return 26; }
    @Override protected int defaultX(int sw, int sh) { return 6; }
    @Override protected int defaultY(int sw, int sh) { return 26; }

    @Override
    public void onEnable() {
        lastPos = null;
        speed = 0f;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        Vec3d pos = mc.player.getPos();
        if (lastPos != null) {
            double dx = pos.x - lastPos.x, dz = pos.z - lastPos.z;
            float now = (float) Math.sqrt(dx * dx + dz * dz) * 20f;
            speed += (now - speed) * 0.2f;
        }
        lastPos = pos;

        if (mc.player.age % 4 != 0) return;
        line1 = "XYZ  " + (int) Math.floor(pos.x) + "  " + (int) Math.floor(pos.y) + "  " + (int) Math.floor(pos.z);
        int tenths = Math.round(speed * 10f);
        line2 = mc.player.getHorizontalFacing().name().charAt(0) + "  |  " + (tenths / 10) + "." + (tenths % 10) + " b/s";
        width = Math.max(mc.textRenderer.getWidth(line1), mc.textRenderer.getWidth(line2));
    }

    @Override
    protected void renderAt(DrawContext context, int x, int y) {
        Render2D.panel(context, x, y, getWidth(), getHeight());
        context.drawTextWithShadow(mc.textRenderer, line1, x + 7, y + 4, Colors.text());
        context.drawTextWithShadow(mc.textRenderer, line2, x + 7, y + 15, Colors.muted());
    }
}
