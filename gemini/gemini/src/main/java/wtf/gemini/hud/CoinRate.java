package wtf.gemini.hud;

import net.minecraft.client.gui.DrawContext;
import wtf.gemini.holyworld.HolyWorldApi;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

import java.util.Locale;

/**
 * Курс обмена монет с публичного REST API HolyWorld.
 *
 * <p>Панель работает только на серверах HolyWorld: вне них запросы к API не уходят вообще.
 * Данные обновляются раз в минуту — публичные методы не имеют лимитов, но и долбить их смысла нет.
 */
public class CoinRate extends HudModule {
    private static final int REFRESH_TICKS = 20 * 60;
    private static final int UP_COLOR = 0xFF4ADE80;
    private static final int DOWN_COLOR = 0xFFF87171;

    private String value = "—";
    private String trend = "";
    private int trendColor = Colors.muted();
    private int width = 56;

    private int ticks;
    /** Время последней сделки, для которой уже пересобрана строка. */
    private long renderedAt = -1;

    public CoinRate() {
        super("CoinRate", Category.HUD, "Курс монет HolyWorld (api.holyworld.me)");
    }

    @Override public int getWidth() { return width + 14; }

    @Override public int getHeight() { return 26; }

    @Override protected int defaultX(int sw, int sh) { return 6; }

    @Override protected int defaultY(int sw, int sh) { return 54; }

    @Override
    public void onEnable() {
        ticks = 0;
        renderedAt = -1;
    }

    @Override
    public void onTick() {
        if (!HolyWorldApi.isOnHolyWorld()) return;

        if (--ticks <= 0) {
            ticks = REFRESH_TICKS;
            HolyWorldApi.requestCoinTrades();
        }
        refresh();
    }

    /** Пересобирает строку только когда пришли новые данные — в кадре аллокаций нет. */
    private void refresh() {
        long stamp = HolyWorldApi.getLastTradeAt();
        if (stamp == renderedAt) return;
        renderedAt = stamp;

        double rate = HolyWorldApi.getLastRate();
        if (Double.isNaN(rate)) {
            value = "—";
            trend = "";
            return;
        }

        value = formatRate(rate);
        double previous = HolyWorldApi.getPreviousRate();
        if (Double.isNaN(previous) || previous == rate) {
            trend = "";
            trendColor = Colors.muted();
        } else if (rate > previous) {
            trend = "▲";
            trendColor = UP_COLOR;
        } else {
            trend = "▼";
            trendColor = DOWN_COLOR;
        }
        width = Math.max(56, mc.textRenderer.getWidth("Coins " + value + " " + trend));
    }

    private static String formatRate(double rate) {
        if (rate >= 1_000_000d) return String.format(Locale.ROOT, "%.2fM", rate / 1_000_000d);
        if (rate >= 1_000d) return String.format(Locale.ROOT, "%.1fK", rate / 1_000d);
        return String.format(Locale.ROOT, "%.0f", rate);
    }

    @Override
    protected void renderAt(DrawContext context, int x, int y) {
        Render2D.panel(context, x, y, getWidth(), getHeight());
        context.drawTextWithShadow(mc.textRenderer, "Coins", x + 7, y + 4, Colors.muted());
        context.drawTextWithShadow(mc.textRenderer, value, x + 7, y + 15, Colors.text());
        if (!trend.isEmpty()) {
            context.drawTextWithShadow(mc.textRenderer, trend,
                    x + 7 + mc.textRenderer.getWidth(value) + 4, y + 15, trendColor);
        }
    }
}
