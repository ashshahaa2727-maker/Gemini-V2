package wtf.gemini.notification;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class NotificationManager {
    /** Короткое сообщение над хотбаром. */
    public void notify(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.inGameHud != null) {
            mc.inGameHud.setOverlayMessage(Text.literal("[Gemini] " + message), false);
        }
    }
}
