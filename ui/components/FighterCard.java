package javawzgdyx.ui.components;

import javawzgdyx.model.CharacterClass;
import javawzgdyx.model.Role;
import javawzgdyx.ui.Theme;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.Random;

/** 战斗界面的角色卡：生命条动画、受击震动、漂浮伤害数字与当前回合高亮。 */
public class FighterCard extends JComponent {

    private final Random random = new Random();

    private Role role;
    private String sideLabel = "PLAYER";
    private boolean enemySide;
    private boolean active;

    private double displayedLife;
    private int targetLife;
    private Timer hpTimer;

    private long flashAt = -10_000;
    private long floatAt = -10_000;
    private boolean flashHeal;
    private String floatingText;
    private Color floatingColor;
    private int floatOffsetX;
    private Timer fxTimer;

    public FighterCard() {
        setOpaque(false);
        setPreferredSize(new Dimension(470, 230));
        setMinimumSize(new Dimension(260, 210));
    }

    public void setRole(Role role, String sideLabel, boolean enemySide) {
        this.role = role;
        this.sideLabel = sideLabel;
        this.enemySide = enemySide;
        this.active = false;
        if (role != null) {
            this.displayedLife = role.getLife();
            this.targetLife = role.getLife();
        }
        this.flashAt = -10_000;
        this.floatAt = -10_000;
        repaint();
    }

    public Role getRole() {
        return role;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            repaint();
        }
    }

    public void refresh() {
        if (role == null) {
            return;
        }
        targetLife = role.getLife();
        ensureHpTimer();
        repaint();
    }

    public void flashDamage(int damage) {
        long now = System.currentTimeMillis();
        flashAt = now;
        floatAt = now;
        flashHeal = false;
        floatingText = "-" + damage;
        floatingColor = Theme.RED;
        floatOffsetX = random.nextInt(61) - 30;
        startFxTimer();
        repaint();
    }

    public void flashHeal(int amount) {
        long now = System.currentTimeMillis();
        flashAt = now;
        floatAt = now;
        flashHeal = true;
        floatingText = "+" + amount;
        floatingColor = Theme.GREEN;
        floatOffsetX = random.nextInt(41) - 20;
        startFxTimer();
        repaint();
    }

    private void ensureHpTimer() {
        if (hpTimer == null) {
            hpTimer = new Timer(16, event -> {
                double diff = targetLife - displayedLife;
                if (Math.abs(diff) < 0.05) {
                    displayedLife = targetLife;
                    hpTimer.stop();
                } else {
                    displayedLife += diff * 0.22;
                }
                repaint();
            });
        }
        if (!hpTimer.isRunning()) {
            hpTimer.start();
        }
    }

    private void startFxTimer() {
        if (fxTimer == null) {
            fxTimer = new Timer(33, event -> {
                long now = System.currentTimeMillis();
                repaint();
                if (now - flashAt > 520 && now - floatAt > 980) {
                    fxTimer.stop();
                }
            });
        }
        if (!fxTimer.isRunning()) {
            fxTimer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        long now = System.currentTimeMillis();

        double shake = 0;
        if (now - flashAt < 320) {
            double progress = (now - flashAt) / 320.0;
            shake = Math.sin((now - flashAt) / 16.0) * 4.5 * (1 - progress);
        }
        g2.translate(shake, 0);

        int width = getWidth();
        int height = getHeight();
        if (role == null) {
            g2.setColor(Theme.alpha(Color.WHITE, 18));
            g2.drawRoundRect(0, 0, width - 1, height - 1, 20, 20);
            g2.dispose();
            return;
        }

        CharacterClass characterClass = role.getCharacterClass();
        Color accent = Theme.accentFor(characterClass);
        boolean damageFlash = now - flashAt < 460 && !flashHeal;
        boolean healFlash = now - flashAt < 460 && flashHeal;

        // 底色
        g2.setColor(Theme.alpha(accent, 12));
        g2.fillRoundRect(0, 0, width, height, 20, 20);
        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(80, 30),
                Math.max(width, height) * 0.9f,
                new float[]{0f, 1f},
                new Color[]{Theme.alpha(accent, 26), new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE
        ));
        g2.fillRoundRect(0, 0, width, height, 20, 20);
        g2.setPaint(new GradientPaint(0, 0, new Color(0x1A2033), 0, height, new Color(0x090C15)));
        g2.fillRoundRect(0, 0, width, height, 20, 20);

        // 边框 / 高亮
        if (active) {
            for (int i = 4; i >= 1; i--) {
                g2.setColor(Theme.alpha(Theme.GOLD, 8));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(-i, -i, width + i * 2 - 1, height + i * 2 - 1, 20 + i, 20 + i);
            }
        }
        Color border;
        if (damageFlash) {
            border = Theme.alpha(Theme.RED, 230);
        } else if (healFlash) {
            border = Theme.alpha(Theme.GREEN, 220);
        } else if (active) {
            border = Theme.alpha(Theme.GOLD, 190);
        } else {
            border = Theme.LINE_STRONG;
        }
        g2.setColor(border);
        g2.setStroke(new BasicStroke(active || damageFlash ? 1.6f : 1f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 20, 20);

        // 头像徽记
        int sigilSize = 64;
        int sigilX = 22;
        int sigilY = 22;
        g2.setColor(Theme.alpha(accent, 30));
        g2.fillRoundRect(sigilX, sigilY, sigilSize, sigilSize, 18, 18);
        g2.setColor(Theme.alpha(accent, 130));
        g2.drawRoundRect(sigilX, sigilY, sigilSize, sigilSize, 18, 18);
        Theme.drawCentered(g2, characterClass.sigil(), new Font("KaiTi", Font.BOLD, 32),
                accent, sigilX + sigilSize / 2, sigilY + 44);

        // 身份与姓名
        g2.setFont(Theme.tracked(Theme.MONO, 0.2f));
        g2.setColor(Theme.alpha(accent, 190));
        g2.drawString(sideLabel, 100, 34);

        g2.setFont(new Font("KaiTi", Font.BOLD, 27));
        g2.setColor(Theme.TEXT);
        FontMetrics nameMetrics = g2.getFontMetrics();
        String displayName = role.getName();
        int nameMax = width - 100 - 24;
        while (!displayName.isEmpty() && nameMetrics.stringWidth(displayName) > nameMax) {
            displayName = displayName.substring(0, displayName.length() - 1);
        }
        if (!displayName.equals(role.getName())) {
            displayName = displayName.substring(0, Math.max(0, displayName.length() - 1)) + "…";
        }
        g2.drawString(displayName, 100, 68);

        // 职业 / 性别标签
        Font tagFont = Theme.UI_TINY.deriveFont(10f);
        g2.setFont(tagFont);
        FontMetrics tagMetrics = g2.getFontMetrics();
        int classWidth = tagMetrics.stringWidth(characterClass.displayName()) + 18;
        Theme.drawPill(g2, characterClass.displayName(), tagFont, 100, 95,
                accent, Theme.alpha(accent, 24), Theme.alpha(accent, 80));
        String sexText = role.getSex() == '女' ? "女性" : "男性";
        Theme.drawPill(g2, sexText, tagFont, 100 + classWidth + 7, 95,
                Theme.MUTED, new Color(255, 255, 255, 7), Theme.alpha(Color.WHITE, 30));

        // 生命值
        g2.setFont(Theme.UI_SMALL);
        g2.setColor(Theme.MUTED);
        g2.drawString("生命值", 24, 130);

        String hpText = Math.max(0, (int) Math.round(displayedLife)) + " / " + role.getMaxLife();
        g2.setFont(Theme.tracked(Theme.UI_BOLD_SMALL, 0.02f));
        g2.setColor(Theme.TEXT);
        FontMetrics hpMetrics = g2.getFontMetrics();
        g2.drawString(hpText, width - 24 - hpMetrics.stringWidth(hpText), 130);

        int barX = 24;
        int barY = 140;
        int barWidth = width - 48;
        int barHeight = 11;
        g2.setColor(new Color(255, 255, 255, 15));
        g2.fillRoundRect(barX, barY, barWidth, barHeight, barHeight, barHeight);

        double ratio = role.getMaxLife() <= 0 ? 0 : displayedLife / role.getMaxLife();
        double shownRatio = Math.max(0, Math.min(1, ratio));
        int fillWidth = (int) Math.round(barWidth * shownRatio);
        if (fillWidth > 0) {
            Color hp = Theme.hpColor(enemySide, ratio);
            g2.setPaint(new GradientPaint(barX, barY, Theme.mix(hp, Color.WHITE, 0.18f),
                    barX, barY + barHeight, hp));
            g2.fillRoundRect(barX, barY, fillWidth, barHeight, barHeight, barHeight);
            g2.setColor(new Color(255, 255, 255, 42));
            g2.fillRoundRect(barX + 4, barY + 2, Math.max(0, fillWidth - 8), 2, 2, 2);
        }
        if (ratio > 1) {
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(1f));
            for (int x = barX + 6; x < barX + barWidth; x += 9) {
                g2.drawLine(x, barY + 3, x - 5, barY + barHeight - 2);
            }
        }
        g2.setColor(new Color(255, 255, 255, 18));
        g2.drawRoundRect(barX, barY, barWidth, barHeight, barHeight, barHeight);

        // 回合 / 状态
        int count = role.getCount();
        String roundText = characterClass == CharacterClass.ZAYU ? "无回合限制" : "回合 " + count;
        Font pillFont = Theme.UI_TINY.deriveFont(Font.BOLD, 10.5f);
        Color pillFg;
        Color pillBg;
        Color pillBorder;
        if (characterClass != CharacterClass.ZAYU && count <= 0) {
            pillFg = Theme.RED;
            pillBg = Theme.alpha(Theme.RED, 22);
            pillBorder = Theme.alpha(Theme.RED, 110);
        } else if (characterClass != CharacterClass.ZAYU && count <= 5) {
            pillFg = Theme.AMBER;
            pillBg = Theme.alpha(Theme.AMBER, 16);
            pillBorder = Theme.alpha(Theme.AMBER, 80);
        } else {
            pillFg = Theme.MUTED;
            pillBg = new Color(255, 255, 255, 7);
            pillBorder = Theme.alpha(Color.WHITE, 30);
        }
        Theme.drawPill(g2, roundText, pillFont, 24, 186, pillFg, pillBg, pillBorder);

        String status = statusText();
        g2.setFont(Theme.UI_TINY);
        g2.setColor(statusColor());
        FontMetrics statusMetrics = g2.getFontMetrics();
        g2.drawString(status, width - 24 - statusMetrics.stringWidth(status), 186);

        // 漂浮数字
        if (floatingText != null && now - floatAt < 950) {
            double progress = (now - floatAt) / 950.0;
            int alpha = (int) (255 * (1 - progress));
            float y = (float) (102 - progress * 58);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha / 255f)));
            g2.setFont(new Font("KaiTi", Font.BOLD, 31));
            FontMetrics floatMetrics = g2.getFontMetrics();
            int floatX = (int) (width * 0.72 + floatOffsetX);
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(floatingText, floatX + 2 - floatMetrics.stringWidth(floatingText) / 2, y + 2);
            g2.setColor(floatingColor);
            g2.drawString(floatingText, floatX - floatMetrics.stringWidth(floatingText) / 2, y);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        g2.dispose();
    }

    @Override
    public void removeNotify() {
        if (hpTimer != null) {
            hpTimer.stop();
        }
        if (fxTimer != null) {
            fxTimer.stop();
        }
        super.removeNotify();
    }

    private String statusText() {
        CharacterClass characterClass = role.getCharacterClass();
        return switch (characterClass) {
            case BUSIZHE -> role.getSkills().stream().anyMatch(skill -> skill.id().equals("revive"))
                    ? "复活尚在 ×1" : "复活已用";
            case LIJIACHENG -> "混沌 · 食油饼可回血";
            case ZAYU -> "基础攻击 0~39";
        };
    }

    private Color statusColor() {
        CharacterClass characterClass = role.getCharacterClass();
        if (characterClass == CharacterClass.BUSIZHE
                && role.getSkills().stream().anyMatch(skill -> skill.id().equals("revive"))) {
            return Theme.alpha(Theme.GOLD, 200);
        }
        return Theme.MUTED;
    }
}
