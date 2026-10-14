package wtf.gemini.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import wtf.gemini.Module;
import wtf.gemini.config.ConfigManager;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

import java.util.List;

/** Перетаскивание HUD-элементов мышью, пока открыт чат (клавиша T). */
public final class HudEditor {
    private static HudModule dragging;
    private static int offX, offY;
    private static boolean wasDown;

    private HudEditor() {}

    public static boolean isEditing() {
        return MinecraftClient.getInstance().currentScreen instanceof ChatScreen;
    }

    public static void render(DrawContext ctx, List<Module> modules) {
        MinecraftClient mc = MinecraftClient.getInstance();
        // При скрытом HUD (F1) редактор тоже не показываем — иначе рамки висели бы без элементов.
        if (!isEditing() || mc.options.hudHidden) {
            dragging = null;
            wasDown = false;
            return;
        }

        double scale = mc.getWindow().getScaleFactor();
        int mx = (int) (mc.mouse.getX() / scale);
        int my = (int) (mc.mouse.getY() / scale);
        boolean down = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();

        if (down && !wasDown) { // нажали: берём верхний элемент под курсором
            for (int i = modules.size() - 1; i >= 0; i--) {
                if (modules.get(i) instanceof HudModule h && h.isEnabled() && h.contains(mx, my)) {
                    dragging = h;
                    offX = mx - h.getX();
                    offY = my - h.getY();
                    break;
                }
            }
        }
        if (down && dragging != null) {
            dragging.setPos(mx - offX, my - offY);
            dragging.clamp(sw, sh); // та же формула, что и при отрисовке
        }
        if (!down && wasDown && dragging != null) { // отпустили: помечаем конфиг изменённым
            ConfigManager.markDirty();
            dragging = null;
        }
        wasDown = down;

        for (Module m : modules) {
            if (m instanceof HudModule h && h.isEnabled() && h.isPlaced()) {
                int color = h == dragging ? 0xFFFFFFFF : 0x99000000 | (Colors.accent() & 0xFFFFFF);
                Render2D.outline(ctx, h.getX() - 1, h.getY() - 1, h.getWidth() + 2, h.getHeight() + 2, color);
            }
        }
        ctx.drawCenteredTextWithShadow(mc.textRenderer, "Перетаскивайте элементы мышью", sw / 2, 30, Colors.accent());
    }
}
