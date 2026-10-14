package wtf.gemini.holyworld;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import wtf.gemini.Gemini;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Клиент публичного REST API HolyWorld ({@code https://api.holyworld.me}).
 *
 * <p>Все методы — {@code GET} и не требуют авторизации. Запросы уходят в отдельном потоке
 * ({@link HttpClient#sendAsync}), результат складывается в volatile-поля, которые читает
 * поток рендера. Данные запрашиваются только когда игрок действительно на сервере HolyWorld.
 */
public final class HolyWorldApi {
    private static final String BASE = "https://api.holyworld.me";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final AtomicBoolean IN_FLIGHT = new AtomicBoolean();

    private static volatile double lastRate = Double.NaN;
    private static volatile double previousRate = Double.NaN;
    /** Время последней сделки по данным сервера — меняется только при новых данных. */
    private static volatile long lastTradeAt;

    private HolyWorldApi() {
    }

    /** @return true, если игрок сейчас на сервере HolyWorld — иначе дёргать API незачем. */
    public static boolean isOnHolyWorld() {
        ServerInfo entry = MinecraftClient.getInstance().getCurrentServerEntry();
        if (entry == null || entry.address == null) {
            return false;
        }
        return entry.address.toLowerCase(Locale.ROOT).contains("holyworld");
    }

    public static double getLastRate() {
        return lastRate;
    }

    public static double getPreviousRate() {
        return previousRate;
    }

    public static long getLastTradeAt() {
        return lastTradeAt;
    }

    /** Запрашивает две последние сделки обмена монет. Повторный вызов игнорируется, пока летит предыдущий. */
    public static void requestCoinTrades() {
        if (!IN_FLIGHT.compareAndSet(false, true)) {
            return;
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/v1/coins-trades?limit=2"))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> applyCoinTrades(response.statusCode(), response.body()))
                .exceptionally(error -> {
                    Gemini.LOGGER.warn("[Gemini] HolyWorld API недоступен: {}", error.toString());
                    return null;
                })
                .whenComplete((ignored, error) -> IN_FLIGHT.set(false));
    }

    private static void applyCoinTrades(int status, String body) {
        if (status != 200) {
            Gemini.LOGGER.warn("[Gemini] HolyWorld API вернул HTTP {}", status);
            return;
        }
        try {
            JsonElement parsed = JsonParser.parseString(body);
            if (!parsed.isJsonArray()) {
                return;
            }
            JsonArray trades = parsed.getAsJsonArray();
            if (trades.isEmpty()) {
                return;
            }

            lastRate = trades.get(0).getAsJsonObject().get("rate").getAsDouble();
            previousRate = trades.size() > 1
                    ? trades.get(1).getAsJsonObject().get("rate").getAsDouble()
                    : Double.NaN;
            lastTradeAt = trades.get(0).getAsJsonObject().get("datetime").getAsLong();
        } catch (RuntimeException e) {
            Gemini.LOGGER.warn("[Gemini] HolyWorld API: некорректный ответ: {}", e.getMessage());
        }
    }
}
