package wtf.gemini.modules.impl.render;

import wtf.gemini.Module;
import wtf.gemini.setting.FloatSetting;

/**
 * Показывает невидимых существ полупрозрачной моделью.
 *
 * <p>Работает через {@code LivingEntityRenderer}: ванильный код вообще не рисует модель
 * невидимки, поэтому недостаточно поднять прозрачность — нужно вернуть её в отрисовку.
 *
 * <p>Ограничение системы рендера 1.21.4: рендер-состояние не хранит ссылку на сущность,
 * поэтому отличить игрока от моба внутри миксина нельзя — функция действует на всех
 * живых существ. Для невидимых мобов это тоже полезно.
 */
public class SeeInvisible extends Module {
    public final FloatSetting opacity = new FloatSetting("Opacity", 60f, 10f, 100f, 5f);

    /** Читается из миксина рендера — статика, чтобы не искать модуль в кадре. */
    private static volatile boolean revealing;
    private static volatile int alpha = 153;

    public SeeInvisible() {
        super("SeeInvisible", Category.RENDER, "Невидимые существа видны полупрозрачной моделью");
        addSettings(opacity);
    }

    @Override
    public void onEnable() {
        revealing = true;
        applyOpacity();
    }

    @Override
    public void onDisable() {
        revealing = false;
    }

    @Override
    public void onTick() {
        applyOpacity();
    }

    private void applyOpacity() {
        alpha = Math.round(Math.max(0f, Math.min(100f, opacity.get())) * 2.55f);
    }

    public static boolean isRevealing() {
        return revealing;
    }

    /**
     * Поднимает альфу ванильного цвета модели.
     *
     * <p>У невидимки цвет приходит почти прозрачным, поэтому RGB сохраняем (там бывает
     * подсветка урона), а прозрачность ставим свою. Полностью чёрный цвет заменяем белым,
     * иначе модель слилась бы с фоном.
     */
    public static int tint(int original) {
        int rgb = original & 0xFFFFFF;
        if (rgb == 0) {
            rgb = 0xFFFFFF;
        }
        return ((alpha & 255) << 24) | rgb;
    }
}
