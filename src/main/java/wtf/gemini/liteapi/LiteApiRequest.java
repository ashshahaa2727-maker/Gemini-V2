package wtf.gemini.liteapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.UUID;

/**
 * Запрос «клиент → сервер»: {@code {id, method, payload}}.
 *
 * <p>{@code id} — UUID запроса: сервер вернёт его в ответе, по нему находится ожидающий запрос.
 */
public final class LiteApiRequest {
    private final String id;
    private final String method;
    private final JsonObject payload;

    private LiteApiRequest(String id, String method, JsonObject payload) {
        this.id = id;
        this.method = method;
        this.payload = payload;
    }

    /**
     * Собирает запрос {@code checkFeatures}.
     *
     * @param clientId стабильный идентификатор мода (см. {@link LiteApi#CLIENT_ID})
     * @param features список активных функций, которые нужно проверить
     */
    public static LiteApiRequest checkFeatures(String clientId, Collection<String> features) {
        JsonArray list = new JsonArray();
        for (String feature : features) {
            list.add(feature);
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("client", clientId);
        payload.add("features", list);

        return new LiteApiRequest(UUID.randomUUID().toString(), LiteApi.METHOD_CHECK_FEATURES, payload);
    }

    public String getId() {
        return id;
    }

    /** @return готовое тело пакета — JSON в UTF-8. */
    public String toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("method", method);
        root.add("payload", payload);
        return root.toString();
    }

    @Override
    public String toString() {
        return "LiteApiRequest{id='" + id + "', method='" + method + "'}";
    }
}
