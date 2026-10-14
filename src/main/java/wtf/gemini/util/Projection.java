package wtf.gemini.util;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Проекция мировых координат в экранные.
 *
 * <p>Матрицы кадра доступны только во время отрисовки мира, поэтому они запоминаются
 * в событии отрисовки мира, а используются позже — при отрисовке интерфейса.
 * Такой путь даёт рамку поверх блоков без возни с отключением теста глубины.
 */
public final class Projection {
    private static Matrix4f positionMatrix;
    private static Matrix4f projectionMatrix;
    private static double cameraX, cameraY, cameraZ;

    private Projection() {
    }

    /** Запоминает матрицы текущего кадра. Вызывается из события отрисовки мира. */
    public static void capture(WorldRenderContext context) {
        positionMatrix = new Matrix4f(context.positionMatrix());
        projectionMatrix = new Matrix4f(context.projectionMatrix());
        Vec3d camera = context.camera().getPos();
        cameraX = camera.x;
        cameraY = camera.y;
        cameraZ = camera.z;
    }

    /**
     * Проецирует мировую точку в координаты интерфейса.
     *
     * @return координаты в масштабированном пространстве интерфейса либо {@code null},
     *         если точка за камерой — такую рисовать нельзя
     */
    public static Vector2f toScreen(Vec3d world, int screenWidth, int screenHeight) {
        Matrix4f position = positionMatrix;
        Matrix4f projection = projectionMatrix;
        if (position == null || projection == null) {
            return null;
        }

        Vector4f point = new Vector4f(
                (float) (world.x - cameraX),
                (float) (world.y - cameraY),
                (float) (world.z - cameraZ),
                1.0f);
        point.mul(position);
        point.mul(projection);

        if (point.w() <= 0.0f) {
            return null;
        }
        float ndcX = point.x() / point.w();
        float ndcY = point.y() / point.w();
        return new Vector2f(
                (ndcX * 0.5f + 0.5f) * screenWidth,
                (0.5f - ndcY * 0.5f) * screenHeight);
    }
}
