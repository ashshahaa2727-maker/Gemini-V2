package wtf.gemini;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.gemini.config.ConfigManager;
import wtf.gemini.hud.HudEditor;
import wtf.gemini.liteapi.FeatureGate;
import wtf.gemini.liteapi.LiteApiClient;
import wtf.gemini.notification.NotificationManager;
import wtf.gemini.register.ModuleStorage;
import wtf.gemini.ui.ClickGuiScreen;

public class Gemini implements ClientModInitializer {
    public static final String NAME = "Gemini";

    /** Версия берётся из fabric.mod.json — единый источник истины это gradle.properties. */
    public static final String VERSION = FabricLoader.getInstance()
            .getModContainer("gemini")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("dev");

    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static Gemini INSTANCE;
    public ModuleStorage moduleStorage;
    public NotificationManager notificationManager;
    /** Применяет блок-лист сервера: скрытые функции исчезают из интерфейса. */
    public FeatureGate featureGate;
    public LiteApiClient liteApi;
    public KeyBinding menuKey;

    @Override
    // HudRenderCallback помечен deprecated в пользу HudLayerRegistrationCallback — см. пояснение ниже.
    @SuppressWarnings("deprecation")
    public void onInitializeClient() {
        INSTANCE = this;
        this.notificationManager = new NotificationManager();
        this.moduleStorage = new ModuleStorage();
        this.moduleStorage.init();

        // LiteAPI (HolyWorld In-Game API): выясняем, какие функции заблокированы на сервере.
        this.featureGate = new FeatureGate(moduleStorage);
        this.liteApi = new LiteApiClient(featureGate);
        this.liteApi.register();

        ConfigManager.load(moduleStorage.getModules());

        this.menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gemini.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.gemini"));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Новый сервер — старый блок-лист не действует.
            featureGate.reset();
            liteApi.requestCheck(moduleStorage.getFeatureIds());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            featureGate.reset();
            liteApi.reset();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }
            moduleStorage.tick();
            liteApi.tick();
            ConfigManager.tick();
        });

        // HudRenderCallback устарел в пользу HudLayerRegistrationCallback, но у нас сознательный выбор:
        // тот добавляет слой внутрь ванильного LayeredDrawer, то есть ПОД чат, а HUD-элементы
        // перетаскиваются мышью как раз при открытом чате. Это событие поддерживается и вызывается
        // после отрисовки ванильного HUD, что и требуется.
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            moduleStorage.renderHud(context);
            HudEditor.render(context, moduleStorage.getVisibleModules());
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save());

        // Отрисовка в мире (3D): траектории снарядов, подсветка целей и всё, чему нужны мировые координаты.
        WorldRenderEvents.LAST.register(moduleStorage::renderWorld);

        LOGGER.info("[Gemini] Initialized with {} modules", moduleStorage.getModules().size());
    }

    /**
     * Всплывающее уведомление над хотбаром.
     *
     * <p>Молчит вне игры: при загрузке конфига игрока ещё нет, и показывать нечего.
     */
    public static void notifyPlayer(String message) {
        if (INSTANCE == null || INSTANCE.notificationManager == null) return;
        if (MinecraftClient.getInstance().player == null) return;
        INSTANCE.notificationManager.notify(message);
    }

    public static Gemini getInstance() {
        return INSTANCE;
    }
}
