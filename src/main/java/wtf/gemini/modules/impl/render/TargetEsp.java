package wtf.gemini.modules.impl.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import wtf.gemini.Module;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.setting.FloatSetting;
import wtf.gemini.setting.ModeSetting;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Projection;
import wtf.gemini.util.Render2D;
import wtf.gemini.util.Render3D;

public class TargetEsp extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", 0, "Box", "Circle", "Crystals");
    public final FloatSetting range = new FloatSetting("Range", 32f, 8f, 64f, 2f);
    public final BooleanSetting playersOnly = new BooleanSetting("Players Only", true);
    public final BooleanSetting showName = new BooleanSetting("Name", true);
    public final BooleanSetting showHealth = new BooleanSetting("Health", true);

    private LivingEntity target;
    private float rotation = 0f;

    public TargetEsp() {
        super("TargetESP", Category.RENDER, "Рамка и круги вокруг цели");
        addSettings(mode, range, playersOnly, showName, showHealth);
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public void onTick() {
        target = findTarget();
        rotation += 5.0f; 
        if (rotation >= 360f) rotation = 0f;
    }

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
    public void onWorldRender(WorldRenderContext context) {
        if (target == null || context.consumers() == null) return;

        int color = Colors.accent();
        int r = (color >> 16) & 255, g = (color >> 8) & 255, b = color & 255;

        Matrix4f matrix = context.matrixStack().peek().getPositionMatrix();
        VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.getLines());

        double dt = context.tickCounter().getTickDelta(false);
        double x = MathHelper.lerp(dt, target.prevX, target.getX()) - context.camera().getPos().x;
        double y = MathHelper.lerp(dt, target.prevY, target.getY()) - context.camera().getPos().y;
        double z = MathHelper.lerp(dt, target.prevZ, target.getZ()) - context.camera().getPos().z;

        if (mode.get().equals("Box")) {
            Box box = target.getBoundingBox().offset(-target.getX(), -target.getY(), -target.getZ()).offset(x, y, z);
            Render3D.boxOutline(buffer, matrix, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, 255);
        } 
        else if (mode.get().equals("Circle") || mode.get().equals("Crystals")) {
            float width = target.getWidth() * 1.2f;
            int segments = 30;
            double heightOffset = (target.getHeight() / 2.0) + (Math.sin(Math.toRadians(rotation)) * (target.getHeight() / 3.0));

            Vec3d prevPoint = null;
            for (int i = 0; i <= segments; i++) {
                double rad = Math.toRadians((i * 360.0 / segments) + rotation);
                double px = x + Math.sin(rad) * width;
                double pz = z + Math.cos(rad) * width;
                Vec3d currentPoint = new Vec3d(px, y + heightOffset, pz);

                if (prevPoint != null) {
                    Render3D.line(buffer, matrix, prevPoint, currentPoint, r, g, b, 255);
                    
                    if (mode.get().equals("Crystals") && i % 4 == 0) {
                        Render3D.line(buffer, matrix, currentPoint, currentPoint.add(0, 0.3, 0), r, g, b, 255);
                        Render3D.line(buffer, matrix, currentPoint, currentPoint.add(0, -0.3, 0), r, g, b, 255);
                    }
                }
                prevPoint = currentPoint;
            }
        }
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (target == null) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        Box box = target.getBoundingBox();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

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

        int accent = Colors.accent();
        if (showHealth.get()) {
            float ratio = Math.max(0f, Math.min(1f,
                    (target.getHealth() + target.getAbsorptionAmount()) / Math.max(1f, target.getMaxHealth())));
            int barY = top + height + 2;
            Render2D.roundedRect(context, left, barY, width, 3, 1, 0x88000000);
            int filled = Math.round(width * ratio);
            if (filled > 0) {
                Render2D.roundedRect(context, left, barY, filled, 3, 1,
                        Colors.lerp(0xFFEF4444, 0xFF22C55E, ratio));
            }
        }

        if (showName.get()) {
            String name = target.getName().getString();
            int textWidth = mc.textRenderer.getWidth(name);
            int textX = left + width / 2 - textWidth / 2;
            int textY = top - 10;
            context.drawTextWithShadow(mc.textRenderer, name, textX, textY, accent);
        }
    }
}
