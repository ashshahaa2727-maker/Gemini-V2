package wtf.gemini.animation;

/**
 * Функция сглаживания: превращает линейный прогресс {@code 0..1} в «красивый».
 *
 * <p>Кривые Безье задаются так же, как в CSS и Figma — {@link #bezier(double, double, double, double)},
 * поэтому сюда можно подставить любую кривую, а не только те, что есть в наборе.
 */
@FunctionalInterface
public interface Easing {
    double apply(double t);

    Easing LINEAR = t -> t;
    Easing SMOOTH_STEP = t -> t * t * (3.0 - 2.0 * t);

    Easing QUAD_OUT = t -> 1.0 - (1.0 - t) * (1.0 - t);
    Easing CUBIC_OUT = t -> 1.0 - Math.pow(1.0 - t, 3);
    Easing QUART_OUT = t -> 1.0 - Math.pow(1.0 - t, 4);
    Easing QUINT_OUT = t -> 1.0 - Math.pow(1.0 - t, 5);

    Easing SINE_OUT = t -> Math.sin(t * Math.PI / 2.0);
    Easing EXPO_OUT = t -> t >= 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * t);
    Easing CIRC_OUT = t -> Math.sqrt(Math.max(0.0, 1.0 - (t - 1.0) * (t - 1.0)));

    /** Проскакивает цель и возвращается — хорошо для тумблеров. */
    Easing BACK_OUT = t -> {
        double c = 1.70158;
        double c3 = c + 1.0;
        return 1.0 + c3 * Math.pow(t - 1.0, 3) + c * Math.pow(t - 1.0, 2);
    };

    Easing ELASTIC_OUT = t -> {
        if (t <= 0.0) return 0.0;
        if (t >= 1.0) return 1.0;
        double period = 0.3;
        return Math.pow(2.0, -10.0 * t) * Math.sin((t - period / 4.0) * (2.0 * Math.PI) / period) + 1.0;
    };

    Easing BOUNCE_OUT = t -> {
        double n = 7.5625;
        double d = 2.75;
        if (t < 1.0 / d) {
            return n * t * t;
        }
        if (t < 2.0 / d) {
            double x = t - 1.5 / d;
            return n * x * x + 0.75;
        }
        if (t < 2.5 / d) {
            double x = t - 2.25 / d;
            return n * x * x + 0.9375;
        }
        double x = t - 2.625 / d;
        return n * x * x + 0.984375;
    };

    /** Мягкое появление панелей. */
    Easing SOFT = bezier(0.25, 0.10, 0.25, 1.00);
    /** Симметричная кривая — для смены темы и цвета. */
    Easing IN_OUT = bezier(0.42, 0.00, 0.58, 1.00);
    /** Быстрый старт с лёгким перелётом — для ховера и раскрытия. */
    Easing SNAP = bezier(0.22, 1.00, 0.36, 1.00);

    /**
     * Кривая Безье с опорными точками {@code (0,0)} и {@code (1,1)}.
     *
     * <p>По оси X напрямую не выражается, поэтому время ищется методом Ньютона
     * (8 итераций — как в браузерах), после чего берётся Y.
     */
    static Easing bezier(double x1, double y1, double x2, double y2) {
        return t -> {
            if (t <= 0.0) return 0.0;
            if (t >= 1.0) return 1.0;

            double u = t;
            for (int i = 0; i < 8; i++) {
                double error = curveX(u, x1, x2) - t;
                if (Math.abs(error) < 1.0e-5) break;
                double slope = curveDX(u, x1, x2);
                if (slope == 0.0) break;
                u = Math.max(0.0, Math.min(1.0, u - error / slope));
            }
            return curveY(u, y1, y2);
        };
    }

    private static double curveX(double u, double x1, double x2) {
        double v = 1.0 - u;
        return 3.0 * v * v * u * x1 + 3.0 * v * u * u * x2 + u * u * u;
    }

    private static double curveDX(double u, double x1, double x2) {
        double v = 1.0 - u;
        return 3.0 * v * v * x1 + 6.0 * v * u * (x2 - x1) + 3.0 * u * u * (1.0 - x2);
    }

    private static double curveY(double u, double y1, double y2) {
        double v = 1.0 - u;
        return 3.0 * v * v * u * y1 + 3.0 * v * u * u * y2 + u * u * u;
    }
}
