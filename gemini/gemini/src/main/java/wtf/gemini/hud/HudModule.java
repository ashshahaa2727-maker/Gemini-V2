package wtf.gemini.hud;

import net.minecraft.client.gui.DrawContext;
import wtf.gemini.Module;

/** HUD-элемент с позицией, которую можно менять перетаскиванием (с открытым чатом). */
public abstract class HudModule extends Module {
    private int x, y;
    private boolean placed;

    protected HudModule(String name, Category category, String description) {
        super(name, category, description);
    }

    public abstract int getWidth();

    public abstract int getHeight();

    protected abstract int defaultX(int screenW, int screenH);

    protected abstract int defaultY(int screenW, int screenH);

    protected abstract void renderAt(DrawContext context, int x, int y);

    @Override
    public final void onHudRender(DrawContext context) {
        int sw = context.getScaledWindowWidth(), sh = context.getScaledWindowHeight();
        if (!placed) {
            x = defaultX(sw, sh);
            y = defaultY(sw, sh);
            placed = true;
        }
        clamp(sw, sh);
        renderAt(context, x, y);
    }

    /**
     * Держит элемент в границах экрана.
     *
     * <p>Единственное место, где живёт эта формула: и отрисовка, и перетаскивание
     * вызывают её, поэтому правила не могут разойтись.
     */
    public void clamp(int screenW, int screenH) {
        x = Math.max(0, Math.min(x, Math.max(0, screenW - getWidth())));
        y = Math.max(0, Math.min(y, Math.max(0, screenH - getHeight())));
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public boolean isPlaced() { return placed; }

    public void setPos(int nx, int ny) {
        this.x = nx;
        this.y = ny;
        this.placed = true;
    }

    public boolean contains(int mx, int my) {
        return placed && mx >= x && mx < x + getWidth() && my >= y && my < y + getHeight();
    }
}
