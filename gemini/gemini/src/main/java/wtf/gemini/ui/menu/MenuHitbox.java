package wtf.gemini.ui.menu;

import wtf.gemini.Module;
import wtf.gemini.setting.Setting;

/**
 * Область, которую нарисовал стиль меню.
 *
 * <p>Стиль сам решает, где что стоит, и возвращает эти прямоугольники — благодаря этому
 * обработка кликов одна на все стили, а не дублируется в каждом.
 */
public record MenuHitbox(int x, int y, int w, int h, Module module, Setting setting) {
    public boolean contains(double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
