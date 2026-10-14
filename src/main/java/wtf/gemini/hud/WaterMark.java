package wtf.gemini.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

/** Плашка: Gemini | FPS | пинг. Текст обновляется раз в полсекунды. */
public class WaterMark extends HudModule {
    private static final String BRAND = "Gemini";
    private String info = "  |  0 FPS  |  0 ms";
    private int brandWidth = 36;
    private int infoWidth = 90;

    public WaterMark() {
        super("WaterMark", Category.HUD, "Плашка с FPS и пингом");
    }

    @Override public int getWidth() { return brandWidth + infoWidth + 14; }
    @Override public int getHeight() { return 16; }
    @Override protected int defaultX(int sw, int sh) { return 6; }
    @Override protected int defaultY(int sw, int sh) { return 6; }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.age % 10 != 0) return;
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        PlayerListEntry entry = handler == null ? null : handler.getPlayerListEntry(mc.player.getUuid());
        int ping = entry == null ? 0 : entry.getLatency();
        info = "  |  " + mc.getCurrentFps() + " FPS  |  " + ping + " ms";
        infoWidth = mc.textRenderer.getWidth(info);
        brandWidth = mc.textRenderer.getWidth(BRAND);
    }

    @Override
    protected void renderAt(DrawContext context, int x, int y) {
        Render2D.panel(context, x, y, getWidth(), getHeight());
        context.drawTextWithShadow(mc.textRenderer, BRAND, x + 7, y + 4, Colors.accent());
        context.drawTextWithShadow(mc.textRenderer, info, x + 7 + brandWidth, y + 4, Colors.muted());
    }
}
