package wtf.gemini.theme;

import wtf.gemini.animation.Animation;
import wtf.gemini.animation.Easing;

/**
 * Цвет, плавно переходящий в новый.
 *
 * <p>Нужен для смены акцента и тем: интерфейс не «щёлкает» цветом, а перетекает
 * за {@value #DURATION_MILLIS} мс.
 */
public final class ColorAnimation {
    private static final long DURATION_MILLIS = 400L;

    private final Animation progress = new Animation(1.0, DURATION_MILLIS, Easing.IN_OUT);
    private int from;
    private int to;

    public ColorAnimation(int initial) {
        this.from = initial;
        this.to = initial;
        this.progress.snapTo(1.0);
    }

    public void setTarget(int color) {
        if (color == to) {
            return;
        }
        this.from = current();
        this.to = color;
        this.progress.snapTo(0.0);
        this.progress.setTarget(1.0);
    }

    /** Продвигает анимацию и возвращает текущий цвет. Вызывать раз в кадр. */
    public int update() {
        progress.update();
        return current();
    }

    public int current() {
        return Colors.lerp(from, to, (float) progress.get());
    }
}
