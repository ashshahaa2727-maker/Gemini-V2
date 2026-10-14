package wtf.gemini.liteapi;

import java.util.List;
import java.util.Set;

/** Результат обращения к LiteAPI. */
public interface LiteApiListener {
    /**
     * Сервер ответил успешно.
     *
     * @param requested функции, которые мы спрашивали
     * @param blocked   подмножество {@code requested}, заблокированное сервером
     */
    void onBlocklist(Set<String> requested, List<String> blocked);

    /**
     * Ответа не дождались или сервер вернул ошибку.
     *
     * @param reason короткое описание причины (для лога)
     */
    default void onFailure(String reason) {
    }
}
