package wtf.gemini.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import wtf.gemini.Module.Category;
import wtf.gemini.util.Render2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PotionsHUD extends HudModule {

    public PotionsHUD() {
        // Если категории HUD у вас нет в enum Category, укажите Category.RENDER
        super("Potions", Category.HUD, "Отображает список активных эффектов зелий");
    }

    @Override
    public void renderAt(DrawContext context, int x, int y) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
        if (effects.isEmpty()) {
            return;
        }

        // Сортировка по убыванию времени действия
        List<StatusEffectInstance> sortedEffects = new ArrayList<>(effects);
        sortedEffects.sort(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed());

        int yOffset = 0;
        int maxWidth = 110;

        for (StatusEffectInstance effectInstance : sortedEffects) {
            RegistryEntry<StatusEffect> effect = effectInstance.getEffectType();
            Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effect);

            // Отрисовка подложки через Render2D (ARGB цвет 0x90151515)
            Render2D.drawRound(context, x, y + yOffset, maxWidth, 20, 4, 0x90151515);

            // Отрисовка иконки эффекта для Minecraft 1.21.4 (с RenderLayer::getGuiTextured)
            if (sprite != null) {
                context.drawTexture(
                    RenderLayer::getGuiTextured,
                    sprite.getAtlasId(),
                    x + 2,
                    y + yOffset + 2,
                    (float) sprite.getX(),
                    (float) sprite.getY(),
                    16,
                    16,
                    256,
                    256
                );
            }

            // Название эффекта + римская цифра уровня
            String name = effect.value().getName().getString();
            int amplifier = effectInstance.getAmplifier();
            if (amplifier > 0) {
                name += " " + getRomanAmplifier(amplifier + 1);
            }

            // Оставшееся время (мм:сс)
            String duration = formatDuration(effectInstance);

            // Текст названия (белый 0xFFFFFFFF) и времени (серый 0xFFAAAAAA)
            context.drawTextWithShadow(client.textRenderer, name, x + 22, y + yOffset + 2, 0xFFFFFFFF);
            context.drawTextWithShadow(client.textRenderer, duration, x + 22, y + yOffset + 11, 0xFFAAAAAA);

            // Подгоняем ширину под самый длинный текст
            int entryWidth = client.textRenderer.getWidth(name) + 28;
            if (entryWidth > maxWidth) {
                maxWidth = entryWidth;
            }

            yOffset += 23;
        }

        // Передаем размеры в HudModule для HudEditor
        this.width = maxWidth;
        this.height = Math.max(yOffset, 20);
    }

    private String formatDuration(StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            return "**:**";
        }
        int totalSeconds = effect.getDuration() / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String getRomanAmplifier(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
}
