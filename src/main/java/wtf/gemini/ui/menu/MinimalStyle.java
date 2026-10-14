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
 * Минималистичный стиль: узкая колонка слева, без панелей и заголовков.
 *
 * <p>Включённый модуль отмечается акцентной полосой слева — так состояние видно,
 * но интерфейс почти не перекрывает игру.
 */
public final class MinimalStyle implements MenuStyle {
    private static final int LEFT = 6, TOP = 34, WIDTH = 132, ROW = 14, SROW = 13;

    @Override
    public List<MenuHitbox> render(DrawContext ctx, ClickGuiScreen menu, int mouseX, int mouseY) {
        List<MenuHitbox> hitboxes = new ArrayList<>();
        List<Module> modules = menu.modules();

        int height = 0;
        for (Module module : modules) {
            height += ROW + 1;
            if (menu.isExpanded(module)) {
                height += module.getSettings().size() * SROW;
            }
        }

        // Полупрозрачная подложка вместо панелей: не отвлекает от игры.
        Render2D.roundedRect(ctx, LEFT - 3, TOP - 3, WIDTH + 6, height + 6, 3,
                Colors.withAlpha(Colors.background(), 0x99));

        int y = TOP;
        for (Module module : modules) {
            hitboxes.add(new MenuHitbox(LEFT, y, WIDTH, ROW, module, null));

            boolean hover = hitboxes.get(hitboxes.size() - 1).contains(mouseX, mouseY);
            if (module.isEnabled()) {
                ctx.fill(LEFT - 2, y + 1, LEFT, y + ROW - 2, Colors.accent());
            } else if (hover) {
                Render2D.roundedRect(ctx, LEFT, y, WIDTH, ROW - 1, 2, Colors.withAlpha(0xFFFFFF, 0x1C));
            }
            ctx.drawTextWithShadow(menu.text(), module.getName(), LEFT + 4, y + 3,
                    module.isEnabled() ? Colors.text() : Colors.muted());
            y += ROW;

            if (menu.isExpanded(module)) {
                for (Setting setting : module.getSettings()) {
                    MenuHitbox box = new MenuHitbox(LEFT + 6, y, WIDTH - 6, SROW, module, setting);
                    hitboxes.add(box);
                    MenuDraw.settingRow(ctx, menu.text(), box.x(), box.y(), box.w(), box.h(),
                            setting, box.contains(mouseX, mouseY));
                    y += SROW;
                }
            }
            y += 1;
        }
        return hitboxes;
    }
}
