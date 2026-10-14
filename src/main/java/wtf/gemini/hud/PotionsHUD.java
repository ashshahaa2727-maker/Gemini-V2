package wtf.gemini.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

import java.util.Collection;

public class PotionsHUD extends HudModule {
    private int width = 100;
    private int height = 24;

    public PotionsHUD() {
        super("PotionsHUD", Category.HUD, "Отображает активные эффекты зелий");
    }

    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    @Override protected int defaultX(int sw, int sh) { return sw - 110; }
    @Override protected int defaultY(int sw, int sh) { return sh / 2 - 50; }

    @Override
    public void onHudRender(DrawContext context) {
        if (mc.player == null) return;

        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        if (effects.isEmpty() && !HudEditor.isEditing()) return;

        int x = getX();
        int y = getY();

        int yOffset = 0;
        int maxWidth = 60;

        // Рисуем подложку, если мы в редакторе (чтобы было за что тянуть) или есть эффекты
        if (effects.isEmpty()) {
            Render2D.panel(context, x, y, 100, 24);
            context.drawTextWithShadow(mc.textRenderer, "Эффекты", x + 5, y + 8, Colors.muted());
            this.width = 100;
            this.height = 24;
            return;
        }

        for (StatusEffectInstance effect : effects) {
            StatusEffect type = effect.getEffectType().value();
            String name = type.getName().getString();
            if (effect.getAmplifier() > 0) {
                name += " " + (effect.getAmplifier() + 1);
            }
            
            int duration = effect.getDuration() / 20;
            String time = String.format("%02d:%02d", duration / 60, duration % 60);
            if (effect.isInfinite()) time = "**:**";

            int currentWidth = 24 + mc.textRenderer.getWidth(name) + 10 + mc.textRenderer.getWidth(time);
            maxWidth = Math.max(maxWidth, currentWidth);

            // Отрисовка фона для конкретного эффекта
            Render2D.panel(context, x, y + yOffset, currentWidth, 18);

            // Отрисовка иконки
            Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(effect.getEffectType());
            context.drawTexture(sprite.getAtlasId(), x + 2, y + yOffset + 1, 0, 16, 16, sprite);

            // Текст
            context.drawTextWithShadow(mc.textRenderer, name, x + 22, y + yOffset + 5, Colors.text());
            context.drawTextWithShadow(mc.textRenderer, time, x + 22 + mc.textRenderer.getWidth(name) + 5, y + yOffset + 5, Colors.muted());

            yOffset += 20;
        }

        this.width = maxWidth;
        this.height = Math.max(20, yOffset);
    }

    @Override
    protected void renderAt(DrawContext context, int x, int y) {
        // Логика отрисовки полностью в onHudRender для динамического изменения размеров
    }
}
