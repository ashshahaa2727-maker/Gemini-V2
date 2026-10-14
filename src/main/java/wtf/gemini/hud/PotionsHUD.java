package wtf.gemini.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PotionsHUD extends HudModule {

    public PotionsHUD() {
        super("Potions", 10, 120, 110, 50);
    }

    /**
     * ВНИМАНИЕ: Если в вашем HudModule абстрактный метод называется draw, 
     * смените render на draw.
     * Если вы убрали final у onHudRender в HudModule.java, можно оставить onHudRender.
     */
    @Override
    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
        if (effects.isEmpty()) {
            return;
        }

        // Сортируем эффекты по оставшемуся времени или названию
        List<StatusEffectInstance> sortedEffects = new ArrayList<>(effects);
        sortedEffects.sort(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed());

        int currentX = (int) this.getX();
        int currentY = (int) this.getY();
        int yOffset = 0;
        int maxWidth = 110;

        for (StatusEffectInstance effectInstance : sortedEffects) {
            RegistryEntry<StatusEffect> effect = effectInstance.getEffectType();
            Sprite sprite = client.getStatusEffectSpriteManager().getSprite(effect);

            // Фон под плашку эффекта (стиль Gemini HUD)
            Render2D.drawRound(context, currentX, currentY + yOffset, maxWidth, 20, 4, Colors.BACKGROUND.getColor());

            // 1. ИСПРАВЛЕНИЕ ОШИБКИ СО СПРАЙТОМ (бывшая строка 66):
            // Используется context.drawSprite без лишнего sprite.getAtlasId()
            if (sprite != null) {
                context.drawSprite(currentX + 2, currentY + yOffset + 2, 0, 16, 16, sprite);
            }

            // Формируем имя эффекта и римскую цифру уровня
            String name = effect.value().getName().getString();
            int amplifier = effectInstance.getAmplifier();
            if (amplifier > 0) {
                name += " " + getRomanAmplifier(amplifier + 1);
            }

            // Форматируем время действия эффекта (мм:сс)
            String duration = formatDuration(effectInstance, client);

            // Отрисовка названия и таймера
            context.drawTextWithShadow(client.textRenderer, name, currentX + 22, currentY + yOffset + 2, Colors.WHITE.getRGB());
            context.drawTextWithShadow(client.textRenderer, duration, currentX + 22, currentY + yOffset + 11, Colors.GRAY.getRGB());

            int entryWidth = client.textRenderer.getWidth(name) + 28;
            if (entryWidth > maxWidth) {
                maxWidth = entryWidth;
            }

            yOffset += 23;
        }

        // Обновляем габариты HUD-элемента для перетаскивания в HudEditor
        this.setWidth(maxWidth);
        this.setHeight(Math.max(yOffset, 20));
    }

    /**
     * Безопасное форматирование времени для разных версий Fabric/Yarn
     */
    private String formatDuration(StatusEffectInstance effect, MinecraftClient client) {
        if (effect.isInfinite()) {
            return "**:**";
        }
        int totalSeconds = effect.getDuration() / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Преобразование уровня эффекта в римские цифры (II, III, IV...)
     */
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
