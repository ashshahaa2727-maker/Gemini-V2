package wtf.gemini.animation;

/**
 * Плавное изменение числового значения во времени.
 *
 * <p>Время берётся из {@link System#nanoTime()}, а не из кадров, поэтому анимация
 * выглядит одинаково при любом FPS — в отличие от «прибавлю 10% за кадр».
 */
public final class Animation {
    private final long durationNanos;

    private Easing easing;
    private double startValue;
    private double targetValue;
    private double value;
    private long startNanos;
    private boolean done;

    public Animation(double initial, long durationMillis, Easing easing) {
        this.durationNanos = Math.max(1L, durationMillis) * 1_000_000L;
        this.easing = easing;
        this.startValue = initial;
        this.targetValue = initial;
        this.value = initial;
        this.done = true;
    }

    public Animation(double initial, long durationMillis) {
        this(initial, durationMillis, Easing.CUBIC_OUT);
    }

    /**
     * Запускает переход к значению от текущего.
     *
     * <p>Повторный вызов с той же целью во время хода анимации ничего не делает —
     * иначе перезапуск сбивал бы плавность на каждом кадре.
     */
    public void setTarget(double target) {
        if (!done && Double.compare(targetValue, target) == 0) {
            return;
        }
        this.startValue = this.value;
        this.targetValue = target;
        this.startNanos = System.nanoTime();
        this.done = false;
    }

    /** Устанавливает значение мгновенно, без анимации. */
    public void snapTo(double target) {
        this.startValue = target;
        this.targetValue = target;
        this.value = target;
        this.done = true;
    }

    /** Продвигает анимацию. Вызывать раз в кадр. */
    public double update() {
        if (done) {
            return value;
        }
        double progress = (double) (System.nanoTime() - startNanos) / durationNanos;
        if (progress >= 1.0) {
            value = targetValue;
            done = true;
            return value;
        }
        value = startValue + (targetValue - startValue) * easing.apply(progress);
        return value;
    }

    public double get() {
        return value;
    }

    public float getFloat() {
        return (float) value;
    }

    public boolean isDone() {
        return done;
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    /**
     * Анимация сглаживания «догоняющего» значения: не имеет конца и просто
     * экспоненциально приближается к цели. Удобна для ховера и скролла.
     */
    public static final class Smooth {
        private double value;
        private final double speed;
        private long lastNanos;

        /**
         * @param speed доля пути за секунду; больше — резче
         */
        public Smooth(double initial, double speed) {
            this.value = initial;
            this.speed = Math.max(0.01, speed);
            this.lastNanos = System.nanoTime();
        }

        public double update(double target) {
            long now = System.nanoTime();
            double dt = Math.min(0.25, (now - lastNanos) / 1_000_000_000.0);
            lastNanos = now;
            // Экспоненциальное сглаживание, независимое от частоты кадров.
            double factor = 1.0 - Math.exp(-speed * dt);
            value += (target - value) * factor;
            return value;
        }

        public double get() {
            return value;
        }

        public float getFloat() {
            return (float) value;
        }

        public void snapTo(double target) {
            this.value = target;
            this.lastNanos = System.nanoTime();
        }
    }
}
