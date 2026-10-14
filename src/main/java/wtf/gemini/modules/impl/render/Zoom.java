package wtf.gemini.modules.impl.render;

import org.lwjgl.glfw.GLFW;
import wtf.gemini.Module;
import wtf.gemini.animation.Animation;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.setting.FloatSetting;

/**
 * Приближение камеры по удержанию клавиши.
 *
 * <p>Множитель читается из {@link #multiplier()} миксином на {@code GameRenderer#getFov}.
 * Значение статическое намеренно: искать модуль по реестру в кадре рендера незачем.
 */
public class Zoom extends Module {
    /** Сглаживание живёт здесь, потому что вызывается чаще тика — раз в кадр. */
    private static final Animation.Smooth SMOOTH = new Animation.Smooth(1.0, 14.0);

    private static volatile boolean zooming;
    private static volatile float level = 4.0f;
    private static volatile boolean smoothZoom = true;

    public final FloatSetting zoomLevel = new FloatSetting("Zoom", 4f, 1.5f, 10f, 0.5f);
    public final BooleanSetting smooth = new BooleanSetting("Smooth", true);

    public Zoom() {
        super("Zoom", Category.RENDER, "Приближение камеры по удержанию клавиши");
        addSettings(zoomLevel, smooth);
    }

    @Override
    public int getDefaultKey() {
        return GLFW.GLFW_KEY_C;
    }

    /** Клавиша удерживается, а не переключается — как в OptiFine. */
    @Override
    public boolean isHoldKey() {
        return true;
    }

    @Override
    public void onEnable() {
        zooming = true;
    }

    @Override
    public void onDisable() {
        zooming = false;
    }

    @Override
    public void onTick() {
        level = zoomLevel.get();
        smoothZoom = smooth.get();
    }

    /**
     * Множитель FOV: {@code 1} — без приближения.
     *
     * <p>Вызывается из потока рендера каждый кадр, поэтому сглаживание считается по времени,
     * а не по тикам — иначе зум выглядел бы ступеньками.
     */
    public static float multiplier() {
        float target = zooming ? level : 1.0f;
        if (!smoothZoom) {
            SMOOTH.snapTo(target);
            return target;
        }
        return (float) SMOOTH.update(target);
    }
}
