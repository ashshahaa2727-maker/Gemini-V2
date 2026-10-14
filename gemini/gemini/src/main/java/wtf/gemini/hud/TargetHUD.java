package wtf.gemini.hud;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import wtf.gemini.theme.Colors;
import wtf.gemini.util.Render2D;

/**
 * Здоровье цели под прицелом (или последней, по кому ударили, до 4 секунд).
 * Невидимые цели не показываются, чтобы HUD не раскрывал невидимок (правила 2.4).
 */
public class TargetHUD extends HudModule {
    private static final int W = 120, H = 36;

    private LivingEntity lastTarget;
    private long lastHit;
    private LivingEntity target, prev;
    private float shownHp = 20f, maxHp = 20f;
    private String name = "";
    private String hpText = "";

    public TargetHUD() {
        super("TargetHUD", Category.HUD, "Здоровье и броня цели");

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity le) {
                lastTarget = le;
                lastHit = System.currentTimeMillis();
            }
            return ActionResult.PASS;
        });
    }

    @Override public int getWidth() { return W; }
    @Override public int getHeight() { return H; }
    @Override protected int defaultX(int sw, int sh) { return sw / 2 + 24; }
    @Override protected int defaultY(int sw, int sh) { return sh / 2 + 24; }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        LivingEntity candidate = null;
        Entity looked = mc.targetedEntity;
        if (looked instanceof LivingEntity le && le.isAlive() && !le.isInvisible()) {
            candidate = le;
        } else if (lastTarget != null && lastTarget.isAlive() && !lastTarget.isRemoved() && !lastTarget.isInvisible()
                && System.currentTimeMillis() - lastHit < 4000 && mc.player.distanceTo(lastTarget) < 16f) {
            candidate = lastTarget;
        }
        target = candidate;
        if (target == null) {
            prev = null;
            return;
        }

        float hp = target.getHealth() + target.getAbsorptionAmount();
        maxHp = Math.max(1f, target.getMaxHealth());
        if (target != prev) {
            shownHp = hp;
            prev = target;
            String n = target.getName().getString();
            name = n.length() > 16 ? n.substring(0, 16) : n;
        } else {
            shownHp += (hp - shownHp) * 0.25f;
        }
        hpText = (Math.round(hp * 10f) / 10f) + " / " + Math.round(maxHp) + "   AR " + target.getArmor();
    }

    @Override
    protected void renderAt(DrawContext c, int x, int y) {
        boolean preview = target == null;
        if (preview && !HudEditor.isEditing()) return;

        String n = preview ? "Target" : name;
        String t = preview ? "20.0 / 20   AR 0" : hpText;
        float hp = preview ? 20f : shownHp;
        float max = preview ? 20f : maxHp;
        float ratio = Math.max(0f, Math.min(1f, hp / max));

        Render2D.panel(c, x, y, W, H);
        c.drawTextWithShadow(mc.textRenderer, n, x + 7, y + 5, Colors.text());

        int bx = x + 7, by = y + 17, bw = W - 14;
        Render2D.roundedRect(c, bx, by, bw, 5, 1, 0x66000000);
        int fw = Math.round(bw * ratio);
        if (fw > 0) {
            Render2D.roundedRect(c, bx, by, fw, 5, 1, Colors.lerp(0xFFEF4444, 0xFF22C55E, ratio));
        }
        c.drawTextWithShadow(mc.textRenderer, t, x + 7, y + 25, Colors.muted());
    }
}
