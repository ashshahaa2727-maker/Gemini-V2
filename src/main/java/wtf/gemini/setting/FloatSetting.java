package wtf.gemini.setting;

public class FloatSetting extends Setting {
    private final float min, max, step;
    private float value;

    public FloatSetting(String name, float value, float min, float max, float step) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = clamp(value);
    }

    public float get() { return value; }
    public float getMin() { return min; }
    public float getMax() { return max; }
    public float getStep() { return step; }

    /**
     * Зажимает значение в {@code [min, max]} и приводит к сетке шага.
     *
     * <p>Инвариант живёт здесь, а не в GUI: иначе значение из конфига обходило бы округление,
     * и один и тот же параметр вёл бы себя по-разному в зависимости от источника.
     */
    public void set(float value) {
        this.value = clamp(value);
    }

    private float clamp(float value) {
        float result = Math.max(min, Math.min(max, value));
        if (step > 0f) {
            result = Math.round(result / step) * step;
            result = Math.max(min, Math.min(max, result));
        }
        return result;
    }
}
