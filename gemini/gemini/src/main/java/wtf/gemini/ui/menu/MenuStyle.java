package wtf.gemini.ui.menu;

import net.minecraft.client.gui.DrawContext;
import wtf.gemini.ui.ClickGuiScreen;

import java.util.List;

/**
 * Стиль меню.
 *
 * <p>Стиль рисует меню и возвращает области строк — обработка ввода остаётся общей
 * и живёт в {@link ClickGuiScreen}.
 */
public interface MenuStyle {
    /**
     * @return области нарисованных строк: по ним экран обрабатывает клики
     */
    List<MenuHitbox> render(DrawContext ctx, ClickGuiScreen menu, int mouseX, int mouseY);
}
