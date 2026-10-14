package wtf.gemini.liteapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Разобранный пакет «сервер → клиент».
 *
 * <p>Это либо ответ на запрос ({@code id} + {@code ok}), либо push-событие
 * ({@code event} без {@code id} и {@code ok}), которое ответом не является.
 */
public final class LiteApiResponse {
    private final String id;
    private final boolean ok;
    private final JsonObject payload;
    private final String error;
    private final String message;

    private LiteApiResponse(String id, boolean ok, JsonObject payload, String error, String message) {
        this.id = id;
        this.ok = ok;
        this.payload = payload;
        this.error = error;
        this.message = message;
    }

    /**
     * Разбирает JSON, пришедший от сервера.
     *
     * @return ответ на запрос либо {@code null}, если это push-событие
     * @throws IllegalArgumentException если тело не является JSON-объектом
     */
    public static LiteApiResponse parse(String json) {
        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(json);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("тело не является валидным JSON", e);
        }
        if (!parsed.isJsonObject()) {
            throw new IllegalArgumentException("ожидался JSON-объект");
        }

        JsonObject root = parsed.getAsJsonObject();
        // Push-события не имеют id и ok — они не ответы на запросы.
        if (!root.has("ok") || root.get("ok").isJsonNull()) {
            return null;
        }

        JsonObject payload = null;
        JsonElement payloadElement = root.get("payload");
        if (payloadElement != null && payloadElement.isJsonObject()) {
            payload = payloadElement.getAsJsonObject();
        }

        return new LiteApiResponse(asString(root, "id"), root.get("ok").getAsBoolean(),
                payload, asString(root, "error"), asString(root, "message"));
    }

    private static String asString(JsonObject root, String key) {
        JsonElement element = root.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    public String getId() {
        return id;
    }

    public boolean isOk() {
        return ok;
    }

    /** Код ошибки: {@code BAD_REQUEST}, {@code NOT_FOUND}, {@code RATE_LIMITED}, {@code INTERNAL_ERROR}. */
    public String getError() {
        return error;
    }

    /** Описание ошибки; при {@code INTERNAL_ERROR} сервер его намеренно не присылает. */
    public String getMessage() {
        return message;
    }

    /**
     * Читает строковый массив из {@code payload[key]}.
     *
     * @return список значений; пустой список, если поля нет или оно не массив
     */
    public List<String> stringList(String key) {
        if (payload == null) {
            return Collections.emptyList();
        }
        JsonElement element = payload.get(key);
        if (element == null || !element.isJsonArray()) {
            return Collections.emptyList();
        }

        JsonArray array = element.getAsJsonArray();
        List<String> values = new ArrayList<>(array.size());
        for (JsonElement item : array) {
            if (item != null && item.isJsonPrimitive()) {
                values.add(item.getAsString());
            }
        }
        return values;
    }

    /** Человекочитаемое описание ошибки для лога. */
    public String describeError() {
        if (message != null && !message.isEmpty()) {
            return error + ": " + message;
        }
        return String.valueOf(error);
    }

    @Override
    public String toString() {
        return ok ? "LiteApiResponse{ok=true}" : "LiteApiResponse{ok=false, error=" + describeError() + "}";
    }
}
