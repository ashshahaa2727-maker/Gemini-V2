package wtf.gemini.modules.impl.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import wtf.gemini.Module;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Предсказание попадания снаряда из руки: лук, арбалет, трезубец, эндер-жемчуг.
 *
 * <p>Физика повторяет ванильную: позиция сдвигается на скорость, затем скорость гасится
 * сопротивлением и к ней применяется гравитация. Различия снарядов сведены в {@link Shot}.
 */
public class Trajectories extends Module {
    private static final int MAX_STEPS = 200;
    /** Расширение проверки попадания по сущностям — снаряд летит быстро и может «проскочить». */
    private static final double ENTITY_MARGIN = 0.3;

    public final BooleanSetting arrows = new BooleanSetting("Arrows", true);
    public final BooleanSetting tridents = new BooleanSetting("Trident", true);
    public final BooleanSetting pearls = new BooleanSetting("Ender Pearl", true);
    public final BooleanSetting marker = new BooleanSetting("Impact Marker", true);

    private final List<Vec3d> path = new ArrayList<>();
    private Vec3d impact;
    private boolean impactOnEntity;

    /** Параметры полёта конкретного снаряда. */
    private record Shot(double speed, double gravity, double drag) {
    }

    public Trajectories() {
        super("Trajectories", Category.RENDER, "Куда попадёт жемчуг, стрела или трезубец");
        addSettings(arrows, tridents, pearls, marker);
    }

    @Override
    public void onDisable() {
        path.clear();
        impact = null;
    }

    @Override
    public void onTick() {
        path.clear();
        impact = null;
        impactOnEntity = false;

        if (mc.player == null || mc.world == null) return;

        ItemStack held = mc.player.getMainHandStack();
        Shot shot = shotFor(held);
        if (shot == null) return;

        Vec3d position = mc.player.getEyePos();
        Vec3d velocity = mc.player.getRotationVector().multiply(shot.speed());

        for (int step = 0; step < MAX_STEPS; step++) {
            Vec3d next = position.add(velocity);

            BlockHitResult blockHit = mc.world.raycast(new RaycastContext(position, next,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
            if (blockHit.getType() != HitResult.Type.MISS) {
                impact = blockHit.getPos();
                path.add(impact);
                return;
            }

            Entity hit = firstEntityHit(position, next);
            if (hit != null) {
                impact = next;
                impactOnEntity = true;
                path.add(impact);
                return;
            }

            path.add(next);
            position = next;

            velocity = velocity.multiply(shot.drag());
            velocity = velocity.subtract(0.0, shot.gravity(), 0.0);
        }

        // Не долетело — показываем хотя бы конец траектории.
        if (!path.isEmpty()) {
            impact = path.get(path.size() - 1);
        }
    }

    /** @return ближайшая живая сущность на отрезке пути, кроме самого игрока */
    private Entity firstEntityHit(Vec3d from, Vec3d to) {
        Box segment = new Box(
                Math.min(from.x, to.x) - ENTITY_MARGIN, Math.min(from.y, to.y) - ENTITY_MARGIN,
                Math.min(from.z, to.z) - ENTITY_MARGIN,
                Math.max(from.x, to.x) + ENTITY_MARGIN, Math.max(from.y, to.y) + ENTITY_MARGIN,
                Math.max(from.z, to.z) + ENTITY_MARGIN);

        for (Entity entity : mc.world.getOtherEntities(mc.player, segment)) {
            if (entity instanceof LivingEntity && entity.isAlive()) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Подбирает параметры полёта по предмету в руке.
     *
     * <p>Гравитация и сопротивление взяты ванильные: у стрел и трезубца 0.05, у жемчуга 0.03.
     */
    private Shot shotFor(ItemStack stack) {
        if (stack.isOf(Items.ENDER_PEARL)) {
            return pearls.get() ? new Shot(1.5, 0.03, 0.99) : null;
        }
        if (stack.isOf(Items.TRIDENT)) {
            return tridents.get() ? new Shot(2.5, 0.05, 0.99) : null;
        }
        if (stack.isOf(Items.CROSSBOW)) {
            return arrows.get() ? new Shot(3.15, 0.05, 0.99) : null;
        }
        if (stack.isOf(Items.BOW)) {
            if (!arrows.get()) return null;
            float pull = bowPull();
            return pull <= 0.05f ? null : new Shot(3.0 * pull, 0.05, 0.99);
        }
        return null;
    }

    /** Ванильная формула натяжения лука: {@code (f^2 + 2f) / 3}, где {@code f = тики / 20}. */
    private float bowPull() {
        if (mc.player == null || !mc.player.isUsingItem()) return 0f;
        float f = mc.player.getItemUseTime() / 20f;
        f = (f * f + f * 2f) / 3f;
        return Math.min(1f, f);
    }

    @Override
    public void onWorldRender(WorldRenderContext context) {
        if (path.size() < 2 || context.consumers() == null) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.getLines());

        int color = Colors.accent();
        int r = (color >> 16) & 255, g = (color >> 8) & 255, b = color & 255;

        for (int i = 0; i < path.size() - 1; i++) {
            // Ближе к концу линия бледнеет — так видно направление полёта.
            int alpha = 220 - 150 * i / Math.max(1, path.size() - 1);
            Render3D.line(buffer, matrix, path.get(i), path.get(i + 1), r, g, b, alpha);
        }

        if (marker.get() && impact != null) {
            int markerColor = impactOnEntity ? 0xFFEF4444 : color;
            Render3D.boxOutline(buffer, matrix,
                    impact.x - 0.15, impact.y - 0.15, impact.z - 0.15,
                    impact.x + 0.15, impact.y + 0.15, impact.z + 0.15,
                    (markerColor >> 16) & 255, (markerColor >> 8) & 255, markerColor & 255, 220);
        }

        matrices.pop();
    }
}
