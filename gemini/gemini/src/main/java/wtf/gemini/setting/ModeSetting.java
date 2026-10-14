package wtf.gemini.setting;

import java.util.Arrays;
import java.util.List;

/** Настройка-переключатель: одно значение из фиксированного списка вариантов. */
public class ModeSetting extends Setting {
    private final List<String> options;
    private int index;

    public ModeSetting(String name, int index, String... options) {
        super(name);
        if (options.length == 0) {
            throw new IllegalArgumentException("ModeSetting без вариантов: " + name);
        }
        this.options = Arrays.asList(options);
        this.index = Math.floorMod(index, options.length);
    }

    public String get() {
        return options.get(index);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int value) {
        this.index = Math.floorMod(value, options.size());
    }

    /** Выбирает вариант по имени; неизвестное имя игнорируется. */
    public void set(String option) {
        int found = options.indexOf(option);
        if (found >= 0) {
            this.index = found;
        }
    }

    public List<String> getOptions() {
        return options;
    }

    public int size() {
        return options.size();
    }

    public void cycle() {
        setIndex(index + 1);
    }
}
