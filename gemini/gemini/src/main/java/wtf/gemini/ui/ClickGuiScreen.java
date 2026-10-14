package wtf.gemini.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import wtf.gemini.Gemini;
import wtf.gemini.Module;
import wtf.gemini.config.ConfigManager;
import wtf.gemini.modules.impl.render.Theme;
import wtf.gemini.setting.BooleanSetting;
import wtf.gemini.setting.ColorSetting;
import wtf.gemini.setting.FloatSetting;
import wtf.gemini.setting.ModeSetting;
import wtf.gemini.setting.Setting;
import wtf.gemini.theme.Colors;
import wtf.gemini.ui.menu.ListStyle;
import wtf.gemini.ui.menu.MenuHitbox;
import wtf.gemini.ui.menu.MenuStyle;
import wtf.gemini.ui.menu.MinimalStyle;
import wtf.gemini.ui.menu.PanelStyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Меню модулей на Right Shift: ЛКМ по модулю — вкл/выкл, ПКМ — настройки.
 *
 * <p>Внешний вид вынесен в {@link MenuStyle}: стиль рисует меню и возвращает области строк,
 * а обработка ввода остаётся здесь и потому одинакова для всех стилей.
 *
 * <p>Показываются только модули, не заблокированные сервером: скрытая функция не должна
 * появляться в меню вообще, вместе со своими настройками.
 */
public class ClickGuiScreen extends Screen {
    /** Раскрытые настройки запоминаются между открытиями меню — так удобнее. */
    private static final Set<Module> EXPANDED = new HashSet<>();

    private static final MenuStyle PANEL = new PanelStyle();
    private static final MenuStyle LIST = new ListStyle();
    private static final MenuStyle MINIMAL = new MinimalStyle();

    private final List<MenuHitbox> hitboxes = new ArrayList<>();
    private final List<Module.Category> categories = new ArrayList<>();

    private String query = "";
    private FloatSetting dragSetting;
    private int dragX, dragW;

    public ClickGuiScreen() {
        super(Text.literal("Gemini"));
    }

    // --- То, что нужно стилям для отрисовки ---

    public TextRenderer text() {
        return textRenderer;
    }

    public List<Module> modules() {
        return Gemini.INSTANCE.moduleStorage.getVisibleModules();
    }

    /** Категории, в которых есть хотя бы один видимый модуль. */
    public List<Module.Category> categories() {
        return categories;
    }

    public boolean isExpanded(Module module) {
        return EXPANDED.contains(module);
    }

    public void toggleExpanded(Module module) {
        if (!EXPANDED.remove(module)) {
            EXPANDED.add(module);
        }
    }

    public String query() {
        return query;
    }

    // --- Внутреннее ---

    private MenuStyle style() {
        Theme theme = Gemini.INSTANCE.moduleStorage.find(Theme.class);
        if (theme == null) {
            return PANEL;
        }
        return switch (theme.menuStyle.getIndex()) {
            case 1 -> LIST;
            case 2 -> MINIMAL;
            default -> PANEL;
        };
    }

    private void refreshCategories() {
        categories.clear();
        List<Module> visible = modules();
        for (Module.Category category : Module.Category.values()) {
            for (Module module : visible) {
                if (module.getCategory() == category) {
                    categories.add(category);
                    break;
                }
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0x88000000); // затемнение без размытия — дешевле для видеокарты
        refreshCategories();

        ctx.drawCenteredTextWithShadow(textRenderer, "Gemini Client " + Gemini.VERSION, width / 2, 10, Colors.accent());
        String hint = style() == LIST
                ? "ЛКМ - вкл/выкл   ПКМ - настройки   Печатайте для поиска"
                : "ЛКМ - вкл/выкл   ПКМ - настройки";
        ctx.drawCenteredTextWithShadow(textRenderer, hint, width / 2, 21, Colors.muted());

        hitboxes.clear();
        hitboxes.addAll(style().render(ctx, this, mx, my));
    }

    private void applySlider(FloatSetting setting, double mouseX) {
        double ratio = (mouseX - dragX) / Math.max(1, dragW);
        ratio = Math.max(0, Math.min(1, ratio));
        setting.set(setting.getMin() + (float) ratio * (setting.getMax() - setting.getMin()));
    }

    /** Переключает модуль и ставит проверку в очередь LiteAPI, если статус ещё не подтверждён. */
    private void toggleModule(Module module) {
        module.toggle();
        if (!module.isEnabled()) return;

        Gemini gemini = Gemini.INSTANCE;
        Set<String> featureIds = gemini.moduleStorage.getFeatureIds();
        if (gemini.featureGate.hasUnverified(featureIds)) {
            gemini.liteApi.requestCheck(featureIds);
        }
    }

    /** Клик по настройке: цвет перебирает палитру клиента. */
    private void clickSetting(Setting setting) {
        if (setting instanceof BooleanSetting bool) {
            bool.toggle();
        } else if (setting instanceof ModeSetting mode) {
            mode.cycle();
        } else if (setting instanceof ColorSetting color) {
            int[] presets = Colors.presets();
            color.set(presets[(Math.max(-1, Colors.accentPresetIndex()) + 1) % presets.length]);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (MenuHitbox box : hitboxes) {
            if (!box.contains(mx, my)) continue;

            if (box.setting() == null) {
                if (button == 0) {
                    toggleModule(box.module());
                } else if (button == 1 && !box.module().getSettings().isEmpty()) {
                    toggleExpanded(box.module());
                }
            } else if (button == 0) {
                Setting setting = box.setting();
                if (setting instanceof FloatSetting number) {
                    dragSetting = number;
                    dragX = box.x() + 6;
                    dragW = box.w() - 12;
                    applySlider(number, mx);
                } else {
                    clickSetting(setting);
                    ConfigManager.markDirty();
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragSetting != null && button == 0) {
            applySlider(dragSetting, mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragSetting != null) {
            dragSetting = null;
            ConfigManager.markDirty();
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        // Поиск нужен только стилю «список».
        if (style() == LIST && chr >= ' ' && chr != 127) {
            query += Character.toLowerCase(chr);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Клавиша меню может быть переназначена — ориентируемся на неё, а не на Right Shift.
        if (Gemini.INSTANCE.menuKey != null && Gemini.INSTANCE.menuKey.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        if (style() == LIST && !query.isEmpty()) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                query = query.substring(0, query.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                query = "";
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        ConfigManager.save();
        super.removed();
    }
}
