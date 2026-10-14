package wtf.gemini.modules.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;
import wtf.gemini.Module;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.setting.FloatSetting;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Projection;
import wtf.gemini.util.Render2D;

/**
 * Рамка вокруг цели: ближайший игрок в радиусе либо тот, на кого смотрит прицел.
 *
 * <p>Рамка рисуется в экранных координатах, поэтому видна и сквозь блоки — в отличие от
 * отрисовки в мире, где линии обрезаются тестом глубины.
 */
public class TargetEsp extends Module {
    public final FloatSetting range = new FloatSetting("Range", 32f, 8f, 64f, 2f);
    public final BooleanSetting playersOnly = new BooleanSetting("Players Only", true);
    public final BooleanSetting showName = new BooleanSetting("Name", true);
    public final BooleanSetting showHealth = new BooleanSetting("Health", true);

    private LivingEntity target;

    public TargetEsp() {
        super("TargetESP", Category.RENDER, "Рамка вокруг цели, видна сквозь блоки");
        addSettings(range, playersOnly, showName, showHealth);
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public void onTick() {
        target = findTarget();
    }

    /** Прицел в приоритете: если игрок целится — показываем именно эту цель. */
    private LivingEntity findTarget() {
        if (mc.player == null || mc.world == null) return null;

        Entity aimed = mc.targetedEntity;
        if (aimed instanceof LivingEntity living && isValid(living)) {
            return living;
        }

        double radius = range.get();
        Box search = mc.player.getBoundingBox().expand(radius);
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;

        for (LivingEntity candidate : mc.world.getEntitiesByClass(LivingEntity.class, search, this::isValid)) {
            double distance = mc.player.squaredDistanceTo(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private boolean isValid(LivingEntity entity) {
        if (entity == mc.player || !entity.isAlive()) return false;
        return !playersOnly.get() || entity instanceof PlayerEntity;
    }

    @Override
    public void onHudRender(DrawContext context) {
        LivingEntity current = target;
        if (current == null) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        Box box = current.getBoundingBox();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        // Проекция всех восьми углов: если хоть один за камерой — рамку не рисуем,
        // иначе она «прилипала» бы к краю экрана и мешала.
        for (int corner = 0; corner < 8; corner++) {
            double x = (corner & 1) == 0 ? box.minX : box.maxX;
            double y = (corner & 2) == 0 ? box.minY : box.maxY;
            double z = (corner & 4) == 0 ? box.minZ : box.maxZ;

            Vector2f point = Projection.toScreen(new Vec3d(x, y, z), screenWidth, screenHeight);
            if (point == null) return;

            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
        }

        int left = Math.round(minX), top = Math.round(minY);
        int width = Math.max(1, Math.round(maxX - minX));
        int height = Math.max(1, Math.round(maxY - minY));

        // Уголки вместо сплошной рамки — меньше перекрывают обзор.
        Render2D.outline(context, left, top, width, height, Colors.accent());

        int accent = Colors.accent();
        if (showHealth.get()) {
            float ratio = Math.max(0f, Math.min(1f,
                    (current.getHealth() + current.getAbsorptionAmount()) / Math.max(1f, current.getMaxHealth())));
            int barY = top + height + 2;
            Render2D.roundedRect(context, left, barY, width, 3, 1, 0x88000000);
            int filled = Math.round(width * ratio);
            if (filled > 0) {
                Render2D.roundedRect(context, left, barY, filled, 3, 1,
                        Colors.lerp(0xFFEF4444, 0xFF22C55E, ratio));
            }
        }

        if (showName.get()) {
            String name = current.getName().getString();
            int textWidth = mc.textRenderer.getWidth(name);
            int textX = left + width / 2 - textWidth / 2;
            int textY = top - 10;
            context.drawTextWithShadow(mc.textRenderer, name, textX, textY, accent);
        }
    }
}
