package wtf.gemini.util;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Примитивы для отрисовки в мире.
 *
 * <p>Всё рисуется в уже существующий буфер ({@code VertexConsumer}) — тот, который выдаёт
 * Fabric в событии отрисовки мира. Своих шейдеров и фреймбуферов не требуется.
 *
 * <p>Координаты — мировые: матрица уже сдвинута на позицию камеры вызывающей стороной.
 */
public final class Render3D {
    private Render3D() {
    }

    /** Отрезок линии. Нормаль нужна формату {@code LINES}. */
    public static void line(VertexConsumer buffer, Matrix4f matrix, Vec3d from, Vec3d to,
                            int r, int g, int b, int a) {
        Vec3d direction = to.subtract(from);
        if (direction.lengthSquared() < 1.0e-8) {
            return;
        }
        Vec3d normal = direction.normalize();
        float nx = (float) normal.x, ny = (float) normal.y, nz = (float) normal.z;

        buffer.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                .color(r, g, b, a).normal(nx, ny, nz);
        buffer.vertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                .color(r, g, b, a).normal(nx, ny, nz);
    }

    /** Каркас параллелепипеда — 12 рёбер одной пачкой. */
    public static void boxOutline(VertexConsumer buffer, Matrix4f matrix,
                                  double minX, double minY, double minZ,
                                  double maxX, double maxY, double maxZ,
                                  int r, int g, int b, int a) {
        double[] xs = {minX, maxX};
        double[] ys = {minY, maxY};
        double[] zs = {minZ, maxZ};

        for (int axis = 0; axis < 3; axis++) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    double[] from = new double[3];
                    double[] to = new double[3];
                    switch (axis) {
                        case 0 -> {
                            from[0] = minX; to[0] = maxX;
                            from[1] = to[1] = ys[i];
                            from[2] = to[2] = zs[j];
                        }
                        case 1 -> {
                            from[1] = minY; to[1] = maxY;
                            from[0] = to[0] = xs[i];
                            from[2] = to[2] = zs[j];
                        }
                        default -> {
                            from[2] = minZ; to[2] = maxZ;
                            from[0] = to[0] = xs[i];
                            from[1] = to[1] = ys[j];
                        }
                    }
                    line(buffer, matrix, new Vec3d(from[0], from[1], from[2]),
                            new Vec3d(to[0], to[1], to[2]), r, g, b, a);
                }
            }
        }
    }
}
