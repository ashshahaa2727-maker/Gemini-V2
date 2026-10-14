package wtf.gemini.liteapi;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import wtf.gemini.Gemini;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Транспорт LiteAPI: отправка {@code checkFeatures} и приём ответов.
 *
 * <p>Лимит модуля — один запрос в 10 секунд на игрока. Здесь он соблюдается сам собой:
 * обращения схлопываются в один отложенный запрос, который уходит, как только окно
 * лимита открылось. Опрос «в цикле» невозможен by design.
 */
public final class LiteApiClient {
    /** Сколько храним запись об отправленном запросе: ответ после таймаута всё ещё принимаем. */
    private static final long OUTSTANDING_TTL_MILLIS = LiteApi.TIMEOUT_MILLIS * 4;

    private record Pending(long sentAt, Set<String> features) {
    }

    private final LiteApiListener listener;
    private final Set<String> queuedFeatures = new LinkedHashSet<>();
    private final Map<String, Pending> outstanding = new HashMap<>();

    private boolean queued;
    /** id запроса, ответа на который мы сейчас ждём; блокирует отправку следующего. */
    private String blockingId;
    private long nextAllowedAt;

    public LiteApiClient(LiteApiListener listener) {
        this.listener = listener;
    }

    /** Объявляет payload-тип в обе стороны и вешает приёмник. Вызывать один раз при инициализации. */
    public void register() {
        // Без регистрации типа клиент не сможет ни собрать исходящий пакет, ни разобрать входящий.
        PayloadTypeRegistry.playC2S().register(LiteApiPayload.FEATURE_CONTROL, LiteApiPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LiteApiPayload.FEATURE_CONTROL, LiteApiPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(LiteApiPayload.FEATURE_CONTROL,
                (payload, context) -> handle(payload.json()));
    }

    /**
     * Ставит функции в очередь на проверку. Повторные вызовы схлопываются:
     * уйдёт один запрос с последним переданным списком.
     */
    public void requestCheck(Collection<String> features) {
        if (features.isEmpty()) {
            return;
        }
        queuedFeatures.clear();
        queuedFeatures.addAll(features);
        queued = true;
    }

    /** Вызывается каждый клиентский тик. */
    public void tick() {
        long now = System.currentTimeMillis();

        if (blockingId != null) {
            Pending pending = outstanding.get(blockingId);
            if (pending != null && now - pending.sentAt() >= LiteApi.TIMEOUT_MILLIS) {
                blockingId = null;
                // Запись в outstanding оставляем: если ответ всё-таки придёт, мы его применим.
                listener.onFailure("timeout");
            }
            return;
        }

        purgeExpired(now);

        if (!queued || now < nextAllowedAt) {
            return;
        }
        if (MinecraftClient.getInstance().getNetworkHandler() == null) {
            return; // не в игре — отправим, когда появится соединение
        }

        send(now);
    }

    private void send(long now) {
        queued = false;

        LiteApiRequest request = LiteApiRequest.checkFeatures(LiteApi.CLIENT_ID, queuedFeatures);
        outstanding.put(request.getId(), new Pending(now, Set.copyOf(queuedFeatures)));
        blockingId = request.getId();
        nextAllowedAt = now + LiteApi.RATE_LIMIT_MILLIS;

        if (Gemini.LOGGER.isDebugEnabled()) {
            Gemini.LOGGER.debug("[Gemini] LiteAPI: checkFeatures {} (server accepts channel: {})",
                    queuedFeatures, ClientPlayNetworking.canSend(LiteApi.FEATURE_CONTROL));
        }
        ClientPlayNetworking.send(new LiteApiPayload(request.toJson()));
    }

    private void handle(String json) {
        LiteApiResponse response;
        try {
            response = LiteApiResponse.parse(json);
        } catch (RuntimeException e) {
            Gemini.LOGGER.warn("[Gemini] LiteAPI: некорректный пакет: {}", e.getMessage());
            return;
        }
        if (response == null) {
            Gemini.LOGGER.debug("[Gemini] LiteAPI: push-событие проигнорировано");
            return;
        }
        if (response.getId() == null) {
            Gemini.LOGGER.warn("[Gemini] LiteAPI: ответ без id");
            return;
        }

        Pending pending = outstanding.remove(response.getId());
        if (pending == null) {
            Gemini.LOGGER.debug("[Gemini] LiteAPI: ответ на неизвестный запрос {}", response.getId());
            return;
        }
        if (response.getId().equals(blockingId)) {
            blockingId = null;
        }

        if (response.isOk()) {
            listener.onBlocklist(pending.features(), response.stringList("blocklist"));
        } else {
            listener.onFailure(response.describeError());
        }
    }

    private void purgeExpired(long now) {
        Iterator<Map.Entry<String, Pending>> it = outstanding.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Pending> entry = it.next();
            if (!entry.getKey().equals(blockingId) && now - entry.getValue().sentAt() > OUTSTANDING_TTL_MILLIS) {
                it.remove();
            }
        }
    }

    /** Сбрасывает состояние при выходе с сервера: лимит считается заново на каждом сервере. */
    public void reset() {
        queuedFeatures.clear();
        outstanding.clear();
        queued = false;
        blockingId = null;
        nextAllowedAt = 0L;
    }
}
