package wtf.gemini.liteapi;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import wtf.gemini.Gemini;
import wtf.gemini.Module;
import wtf.gemini.register.ModuleStorage;
import wtf.gemini.util.GameOptionsAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Применяет блок-лист сервера к модулям.
 *
 * <p>Требование API: заблокированная функция должна быть <b>полностью скрыта</b> от игрока —
 * убрана из меню, настроек, подсказок и любых других UI-элементов. Недостаточно запретить
 * включение. Поэтому модуль не просто выключается, а перестаёт существовать для интерфейса:
 * см. {@link Module#isBlocked()}, фильтры в {@code ClickGuiScreen} и {@code ModuleStorage}.
 *
 * <p>Fail-open: пока сервер не подтвердил блокировку, не скрывается ничего. На серверах без
 * LiteAPI запрос остаётся без ответа, и клиент обязан работать как обычно.
 */
public final class FeatureGate implements LiteApiListener {
    private final ModuleStorage storage;
    private final Set<String> blocked = new HashSet<>();
    private final Set<String> verified = new HashSet<>();

    public FeatureGate(ModuleStorage storage) {
        this.storage = storage;
    }

    @Override
    public void onBlocklist(Set<String> requested, List<String> blockedFeatures) {
        verified.addAll(requested);
        blocked.clear();
        blocked.addAll(blockedFeatures);
        apply();

        if (!blockedFeatures.isEmpty()) {
            Gemini.LOGGER.info("[Gemini] LiteAPI: сервер заблокировал {} функц.: {}",
                    blockedFeatures.size(), blockedFeatures);
        }
    }

    @Override
    public void onFailure(String reason) {
        Gemini.LOGGER.debug("[Gemini] LiteAPI: проверка функций не удалась ({})", reason);
    }

    public boolean isBlocked(Module module) {
        return blocked.contains(module.getFeatureId());
    }

    /** @return true, если статус хотя бы одной из функций ещё не подтверждён сервером. */
    public boolean hasUnverified(Collection<String> featureIds) {
        for (String id : featureIds) {
            if (!verified.contains(id)) {
                return true;
            }
        }
        return false;
    }

    /** Полный сброс: подключение к новому серверу или выход. */
    public void reset() {
        blocked.clear();
        verified.clear();
        apply();
    }

    private void apply() {
        for (Module module : storage.getModules()) {
            module.setBlocked(blocked.contains(module.getFeatureId()));
        }
        storage.invalidateVisible();
        syncKeyBinds();
    }

    /**
     * Пересобирает {@code GameOptions.allKeys}, оставляя только клавиши видимых модулей.
     *
     * <p>Иначе заблокированная функция осталась бы видна в ванильном экране «Управление» —
     * а это ровно тот UI, из которого её требуется убрать.
     */
    private void syncKeyBinds() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) {
            return;
        }
        KeyBinding[] current = mc.options.allKeys;
        if (current == null) {
            return;
        }

        Set<KeyBinding> moduleBindings = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Module module : storage.getModules()) {
            KeyBinding binding = module.getKeyBinding();
            if (binding != null) {
                moduleBindings.add(binding);
            }
        }

        List<KeyBinding> next = new ArrayList<>(current.length);
        for (KeyBinding binding : current) {
            if (!moduleBindings.contains(binding)) {
                next.add(binding);
            }
        }
        for (Module module : storage.getModules()) {
            KeyBinding binding = module.getKeyBinding();
            if (binding != null && !module.isBlocked()) {
                next.add(binding);
            }
        }

        if (sameOrder(current, next)) {
            return;
        }
        GameOptionsAccess.setAllKeys(mc.options, next.toArray(new KeyBinding[0]));
    }

    private static boolean sameOrder(KeyBinding[] current, List<KeyBinding> next) {
        if (current.length != next.size()) {
            return false;
        }
        for (int i = 0; i < current.length; i++) {
            if (current[i] != next.get(i)) {
                return false;
            }
        }
        return true;
    }
}
