package wtf.gemini.ui.menu;

import net.minecraft.client.gui.DrawContext;
import wtf.gemini.Module;
import wtf.gemini.setting.Setting;
import wtf.gemini.theme.Colors;
import wtf.gemini.ui.ClickGuiScreen;
import wtf.gemini.util.Render2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Стиль «список»: одна широкая панель с поиском, модули сгруппированы по категориям.
 *
 * <p>Удобен, когда модулей много: поиск и единая колонка избавляют от беготни глазами
 * по панелям.
 */
public final class ListStyle implements MenuStyle {
    private static final int WIDTH = 260, TOP = 38, HEADER = 24, ROW = 16, SROW = 15, GROUP = 12;

    @Override
    public List<MenuHitbox> render(DrawContext ctx, ClickGuiScreen menu, int mouseX, int mouseY) {
        List<MenuHitbox> hitboxes = new ArrayList<>();
        String query = menu.query();
        int x = (menu.width - WIDTH) / 2;

        // Отбор по поиску: ищем по названию модуля.
        List<Module> matches = new ArrayList<>();
        for (Module module : menu.modules()) {
            if (query.isEmpty() || module.getName().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(module);
            }
        }

        int content = 0;
        Module.Category previous = null;
        for (Module module : matches) {
            if (module.getCategory() != previous) {
                content += GROUP;
                previous = module.getCategory();
            }
            content += ROW + 1;
            if (menu.isExpanded(module)) {
                content += module.getSettings().size() * SROW;
            }
        }

        Render2D.panel(ctx, x, TOP, WIDTH, HEADER + Math.max(ROW, content) + 6);

        String prompt = query.isEmpty() ? "Поиск: печатайте..." : "Поиск: " + query;
        ctx.drawTextWithShadow(menu.text(), prompt, x + 8, TOP + 8, query.isEmpty() ? Colors.muted() : Colors.text());

        int y = TOP + HEADER;
        previous = null;
        for (Module module : matches) {
            if (module.getCategory() != previous) {
                ctx.drawTextWithShadow(menu.text(), module.getCategory().name(), x + 8, y + 3, Colors.muted());
                y += GROUP;
                previous = module.getCategory();
            }

            hitboxes.add(new MenuHitbox(x + 6, y, WIDTH - 12, ROW, module, null));
            y += ROW;

            if (menu.isExpanded(module)) {
                for (Setting setting : module.getSettings()) {
                    hitboxes.add(new MenuHitbox(x + 10, y, WIDTH - 20, SROW, module, setting));
                    y += SROW;
                }
            }
            y += 1;
        }

        if (matches.isEmpty()) {
            ctx.drawTextWithShadow(menu.text(), "Ничего не найдено", x + 8, TOP + HEADER + 2, Colors.muted());
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
        return hitboxes;
    }
}
