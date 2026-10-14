package wtf.gemini.liteapi;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.nio.charset.StandardCharsets;

/**
 * Полезная нагрузка LiteAPI — сырой UTF-8 JSON.
 *
 * <p>Важно: сервер ждёт именно «строку UTF-8 JSON» без какого-либо префикса, поэтому
 * тело пишется напрямую через {@link RegistryByteBuf#writeBytes}. Использовать
 * {@code writeString} нельзя — он добавил бы VarInt-длину и сломал бы разбор на стороне сервера.
 */
public record LiteApiPayload(String json) implements CustomPayload {
    /** Идентификатор payload-типа для канала {@code liteapi:feature-control}. */
    public static final CustomPayload.Id<LiteApiPayload> FEATURE_CONTROL =
            new CustomPayload.Id<>(LiteApi.FEATURE_CONTROL);

    public static final PacketCodec<RegistryByteBuf, LiteApiPayload> CODEC =
            CustomPayload.codecOf(LiteApiPayload::write, LiteApiPayload::new);

    public LiteApiPayload(RegistryByteBuf buf) {
        this(new String(readAll(buf), StandardCharsets.UTF_8));
    }

    private void write(RegistryByteBuf buf) {
        buf.writeBytes(json.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] readAll(RegistryByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return FEATURE_CONTROL;
    }
}
