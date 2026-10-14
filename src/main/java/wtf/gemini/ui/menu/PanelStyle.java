package wtf.gemini.ui.menu;

import net.minecraft.client.gui.DrawContext;
import wtf.gemini.Gemini;
import wtf.gemini.Module;
import wtf.gemini.modules.impl.render.Theme;
import wtf.gemini.setting.Setting;
import wtf.gemini.ui.ClickGuiScreen;
import wtf.gemini.util.Render2D;

import java.util.ArrayList;
import java.util.List;

public final class PanelStyle implements MenuStyle {
    private static final int GAP = 8, TOP = 34, HEADER = 20, ROW = 16, SROW = 15;

    @Override
    public List<MenuHitbox> render(DrawContext ctx, ClickGuiScreen menu, int mouseX, int mouseY) {
        List<MenuHitbox> hitboxes = new ArrayList<>();
        List<Module> modules = menu.modules();
        List<Module.Category> categories = menu.categories();
        if (categories.isEmpty()) return hitboxes;

        // Получаем настройки из модуля Theme
        Theme theme = Gemini.INSTANCE.moduleStorage.get(Theme.class);
        int rad = Math.round(theme.rounding.get());
        int bgC = theme.panelColor.get();
        int outlineC = theme.outlineColor.get();
        boolean doOutline = theme.showOutline.get();

        int columns = categories.size();
        int panelWidth = Math.max(90, Math.min(125, (menu.width - GAP * (columns + 1)) / columns));
        int total = columns * panelWidth + (columns - 1) * GAP;
        int startX = Math.max(GAP, (menu.width - total) / 2);

        for (int c = 0; c < columns; c++) {
            int x = startX + c * (panelWidth + GAP);
            int y = TOP + HEADER + 4;

            for (Module module : modules) {
                if (module.getCategory() != categories.get(c)) continue;

                int rowY = y;
                hitboxes.add(new MenuHitbox(x + 4, rowY, panelWidth - 8, ROW, module, null));
                y += ROW + 2;

                if (menu.isExpanded(module)) {
                    for (Setting setting : module.getSettings()) {
                        hitboxes.add(new MenuHitbox(x + 4, y, panelWidth - 8, SROW, module, setting));
                        y += SROW;
                    }
                    y += 2;
                }
            }

            // Динамический фон панели
            Render2D.roundedRect(ctx, x, TOP, panelWidth, y + 2 - TOP, rad, bgC);
            if (doOutline) {
                Render2D.outline(ctx, x, TOP, panelWidth, y + 2 - TOP, outlineC);
            }
            
            MenuDraw.header(ctx, menu.text(), x, TOP + 6, panelWidth, categories.get(c).name());
        }

        // Отрисовка самих кнопок и ползунков
        for (MenuHitbox box : hitboxes) {
            boolean hover = box.contains(mouseX, mouseY);
            if (box.setting() == null) {
                MenuDraw.moduleRow(ctx, menu.text(), box.x(), box.y(), box.w(), box.h(),
                        box.module(), menu.isExpanded(box.module()), hover);
            } else {
                MenuDraw.settingRow(ctx, menu.text(), box.x(), box.y(), box.w(), box.h(), box.setting(), hover);
            }
        }

        return hitboxes;
    }
}
