package wtf.gemini.ui.menu;

import net.minecraft.client.gui.DrawContext;
import wtf.gemini.Module;
import wtf.gemini.setting.Setting;
import wtf.gemini.theme.Colors;
import wtf.gemini.ui.ClickGuiScreen;
import wtf.gemini.util.Render2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Панельный стиль: своя колонка на каждую категорию.
 *
 * <p>Классическая компоновка клиентов — видно всё сразу, категории не пересекаются.
 */
public final class PanelStyle implements MenuStyle {
    private static final int GAP = 8, TOP = 34, HEADER = 18, ROW = 16, SROW = 15;

    @Override
    public List<MenuHitbox> render(DrawContext ctx, ClickGuiScreen menu, int mouseX, int mouseY) {
        List<MenuHitbox> hitboxes = new ArrayList<>();
        List<Module> modules = menu.modules();
        List<Module.Category> categories = menu.categories();
        if (categories.isEmpty()) return hitboxes;

        int columns = categories.size();
        int panelWidth = Math.max(70, Math.min(110, (menu.width - GAP * (columns + 1)) / columns));
        int total = columns * panelWidth + (columns - 1) * GAP;
        int startX = Math.max(GAP / 2, (menu.width - total) / 2);

        for (int c = 0; c < columns; c++) {
            int x = startX + c * (panelWidth + GAP);
            int y = TOP + HEADER + 2;

            for (Module module : modules) {
                if (module.getCategory() != categories.get(c)) continue;

                int rowY = y;
                hitboxes.add(new MenuHitbox(x + 3, rowY, panelWidth - 6, ROW, module, null));
                y += ROW;

                if (menu.isExpanded(module)) {
                    for (Setting setting : module.getSettings()) {
                        hitboxes.add(new MenuHitbox(x + 3, y, panelWidth - 6, SROW, module, setting));
                        y += SROW;
                    }
                }
                y += 1;
            }

            Render2D.panel(ctx, x, TOP, panelWidth, y + 2 - TOP);
            MenuDraw.header(ctx, menu.text(), x, TOP + 5, panelWidth, categories.get(c).name());
        }

        for (MenuHitbox box : hitboxes) {
            boolean hover = box.contains(mouseX, mouseY);
            if (box.setting() == null) {
                MenuDraw.moduleRow(ctx, menu.text(), box.x(), box.y(), box.w(), box.h(),
                        box.module(), menu.isExpanded(box.module()), hover);
            } else {
                MenuDraw.settingRow(ctx, menu.text(), box.x(), box.y(), box.w(), box.h(), box.setting(), hover);
            }
        }

        // Подсказка по наведённому модулю — под всеми панелями.
        for (MenuHitbox box : hitboxes) {
            if (box.setting() == null && box.contains(mouseX, mouseY)) {
                ctx.drawCenteredTextWithShadow(menu.text(), box.module().getDescription(),
                        menu.width / 2, menu.height - 14, Colors.text());
                break;
            }
        }
        return hitboxes;
    }
}
