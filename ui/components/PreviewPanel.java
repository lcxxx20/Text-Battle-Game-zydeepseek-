package javawzgdyx.ui.components;

import javawzgdyx.model.CharacterClass;
import javawzgdyx.model.Skill;
import javawzgdyx.ui.Theme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.List;

/** 选角表单里的角色预览卡。 */
public class PreviewPanel extends JComponent {

    private CharacterClass characterClass = CharacterClass.BUSIZHE;
    private String name = "";
    private char sex = '男';

    public PreviewPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(420, 200));
        setMinimumSize(new Dimension(260, 180));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
    }

    public void setDraft(CharacterClass characterClass, String name, char sex) {
        this.characterClass = characterClass;
        this.name = name == null ? "" : name;
        this.sex = sex;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        Color accent = Theme.accentFor(characterClass);

        g2.setPaint(new GradientPaint(0, 0, new Color(0x121827), 0, height, new Color(0x090C15)));
        g2.fillRoundRect(0, 0, width, height, 16, 16);

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(30, 20),
                Math.max(width, height) * 0.9f,
                new float[]{0f, 1f},
                new Color[]{Theme.alpha(accent, 30), new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0)},
                java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE
        ));
        g2.fillRoundRect(0, 0, width, height, 16, 16);

        g2.setColor(Theme.alpha(accent, 70));
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                1f, new float[]{5f, 5f}, 0f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 16, 16);

        g2.setFont(Theme.tracked(Theme.MONO, 0.24f));
        g2.setColor(Theme.alpha(accent, 180));
        g2.drawString("CHARACTER PREVIEW", 20, 26);

        int sigilSize = 48;
        int sigilX = 20;
        int sigilY = 40;
        g2.setColor(Theme.alpha(accent, 30));
        g2.fillRoundRect(sigilX, sigilY, sigilSize, sigilSize, 13, 13);
        g2.setColor(Theme.alpha(accent, 120));
        g2.drawRoundRect(sigilX, sigilY, sigilSize, sigilSize, 13, 13);
        Theme.drawCentered(g2, characterClass.sigil(), new Font("KaiTi", Font.BOLD, 26),
                accent, sigilX + sigilSize / 2, sigilY + 35);

        String shownName = name.isBlank() ? "未命名角色" : name;
        g2.setFont(new Font("KaiTi", Font.BOLD, 22));
        g2.setColor(name.isBlank() ? Theme.FAINT : Theme.TEXT);
        FontMetrics nameMetrics = g2.getFontMetrics();
        String clipped = shownName;
        int maxNameWidth = width - 82 - 20;
        while (!clipped.isEmpty() && nameMetrics.stringWidth(clipped) > maxNameWidth) {
            clipped = clipped.substring(0, clipped.length() - 1);
        }
        g2.drawString(clipped, 82, 66);

        g2.setFont(Theme.UI_SMALL);
        g2.setColor(Theme.MUTED);
        g2.drawString(characterClass.displayName() + " · " + (sex == '女' ? "女性" : "男性"), 82, 86);

        g2.setColor(new Color(255, 255, 255, 20));
        g2.drawLine(20, 106, width - 20, 106);

        stat(g2, 20, 126, "初始生命", "100~199");
        stat(g2, 118, 126, "初始回合", characterClass == CharacterClass.ZAYU ? "不限" : "20");
        stat(g2, 216, 126, "技能数量", String.valueOf(characterClass.skills().size()));

        g2.setFont(Theme.UI_TINY);
        g2.setColor(Theme.FAINT);
        g2.drawString("SKILLS", 20, height - 32);

        int x = 78;
        int baseline = height - 31;
        g2.setFont(Theme.UI_TINY.deriveFont(10.5f));
        FontMetrics chipMetrics = g2.getFontMetrics();
        List<Skill> skills = characterClass.skills();
        if (skills.isEmpty()) {
            g2.setColor(Theme.FAINT);
            g2.drawString("基础攻击", x, baseline);
        } else {
            for (Skill skill : skills) {
                int chipWidth = chipMetrics.stringWidth(skill.name()) + 16;
                if (x + chipWidth > width - 20) {
                    break;
                }
                g2.setColor(Theme.alpha(accent, 14));
                g2.fillRoundRect(x, baseline - 13, chipWidth, 20, 20, 20);
                g2.setColor(Theme.alpha(accent, 65));
                g2.drawRoundRect(x, baseline - 13, chipWidth, 20, 20, 20);
                g2.setColor(Theme.MUTED);
                g2.drawString(skill.name(), x + 8, baseline + 1);
                x += chipWidth + 6;
            }
        }

        g2.dispose();
    }

    private void stat(Graphics2D g2, int x, int y, String label, String value) {
        g2.setFont(Theme.UI_TINY);
        g2.setColor(Theme.FAINT);
        g2.drawString(label, x, y);
        g2.setFont(Theme.tracked(Theme.UI_BOLD_SMALL, 0.04f));
        g2.setColor(Theme.TEXT);
        g2.drawString(value, x, y + 18);
    }
}
