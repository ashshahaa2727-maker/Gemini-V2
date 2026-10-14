package wtf.gemini.liteapi;

import net.minecraft.util.Identifier;

/**
 * Константы протокола HolyWorld In-Game API (LiteAPI).
 *
 * <p>Обмен идёт через Minecraft Plugin Messaging: клиент шлёт Serverbound Plugin Message
 * на именованный канал, сервер отвечает Clientbound Plugin Message на тот же канал.
 * Тело пакета — сырая строка UTF-8 с JSON (см. {@link LiteApiPayload}).
 */
public final class LiteApi {
    /** Namespace всех каналов LiteAPI. */
    public static final String NAMESPACE = "liteapi";

    /** Канал модуля Feature Control. */
    public static final Identifier FEATURE_CONTROL = Identifier.of(NAMESPACE, "feature-control");

    /**
     * Стабильный идентификатор мода для персональных блокировок.
     *
     * <p>Это НЕ UUID и НЕ ник игрока: сервер хранит по этой строке персональный блок-лист,
     * поэтому значение не должно меняться между версиями мода.
     */
    public static final String CLIENT_ID = "gemini";

    /** Метод проверки активных функций. */
    public static final String METHOD_CHECK_FEATURES = "checkFeatures";

    /** Лимит модуля: 1 запрос в 10 секунд на игрока. */
    public static final long RATE_LIMIT_MILLIS = 10_000L;

    /** Сколько ждём ответ, прежде чем считать запрос потерянным. */
    public static final long TIMEOUT_MILLIS = 5_000L;

    // Коды ошибок из документации.
    public static final String ERROR_BAD_REQUEST = "BAD_REQUEST";
    public static final String ERROR_NOT_FOUND = "NOT_FOUND";
    public static final String ERROR_RATE_LIMITED = "RATE_LIMITED";
    public static final String ERROR_INTERNAL_ERROR = "INTERNAL_ERROR";

    private LiteApi() {
    }
}
