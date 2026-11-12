package javawzgdyx.ui.components;

import javawzgdyx.model.CharacterClass;
import javawzgdyx.model.Skill;
import javawzgdyx.ui.Theme;

import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.List;

/** 选角界面的大职业卡。整卡可点击，选中态有清晰的金色勾选标记。 */
public class ClassCard extends JToggleButton {

    private final CharacterClass characterClass;
    private final Color accent;

    public ClassCard(CharacterClass characterClass) {
        super(characterClass.displayName());
        this.characterClass = characterClass;
        this.accent = Theme.accentFor(characterClass);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setPreferredSize(new Dimension(560, 236));
        setAlignmentX(LEFT_ALIGNMENT);
        setToolTipText("选择 " + characterClass.displayName() + "：" + characterClass.tagline());
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        boolean selected = isSelected();
        boolean hover = getModel().isRollover();

        g2.setColor(Theme.alpha(accent, selected ? 30 : (hover ? 20 : 12)));
        g2.fillRoundRect(0, 0, width, height, 18, 18);
        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(60, 30),
                Math.max(width, height) * 0.8f,
                new float[]{0f, 1f},
                new Color[]{Theme.alpha(accent, selected ? 40 : 18), new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE
        ));
        g2.fillRoundRect(0, 0, width, height, 18, 18);

        g2.setPaint(new java.awt.GradientPaint(0, 0,
                selected ? new Color(0x20263A) : new Color(0x151B2C),
                0, height, new Color(0x0A0D18)));
        g2.fillRoundRect(0, 0, width, height, 18, 18);

        // 低透明度职业徽记，填充中段留白。
        java.awt.Shape oldClip = g2.getClip();
        g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, 18, 18));
        g2.setFont(new Font("KaiTi", Font.BOLD, 142));
        g2.setColor(Theme.alpha(accent, selected ? 16 : 11));
        FontMetrics watermarkMetrics = g2.getFontMetrics();
        g2.drawString(characterClass.sigil(), width - 44 - watermarkMetrics.stringWidth(characterClass.sigil()),
                height - 58);
        g2.setClip(oldClip);

        g2.setColor(selected ? Theme.alpha(accent, 190) : Theme.alpha(Color.WHITE, hover ? 62 : 30));
        g2.setStroke(new BasicStroke(selected ? 1.6f : 1f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 18, 18);

        // 徽记
        int sigilSize = 48;
        int sigilX = 22;
        int sigilY = 22;
        g2.setColor(Theme.alpha(accent, selected ? 46 : 24));
        g2.fillRoundRect(sigilX, sigilY, sigilSize, sigilSize, 14, 14);
        g2.setColor(Theme.alpha(accent, selected ? 180 : 100));
        g2.drawRoundRect(sigilX, sigilY, sigilSize, sigilSize, 14, 14);
        Theme.drawCentered(g2, characterClass.sigil(), new Font("KaiTi", Font.BOLD, 26),
                accent, sigilX + sigilSize / 2, sigilY + 34);

        // 名称与副标题
        g2.setFont(Theme.tracked(new Font("Microsoft YaHei", Font.BOLD, 19), 0.04f));
        g2.setColor(Theme.TEXT);
        g2.drawString(characterClass.displayName(), 84, 46);
        g2.setFont(Theme.UI_SMALL);
        g2.setColor(Theme.MUTED);
        g2.drawString(characterClass.tagline(), 84, 65);

        // 选中标记
        int checkSize = 22;
        int checkX = width - 24 - checkSize;
        int checkY = 26;
        if (selected) {
            g2.setColor(accent);
            g2.fillOval(checkX, checkY, checkSize, checkSize);
            g2.setColor(new Color(0x0B0F1B));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = checkX + checkSize / 2;
            int cy = checkY + checkSize / 2;
            g2.drawLine(cx - 5, cy, cx - 1, cy + 4);
            g2.drawLine(cx - 1, cy + 4, cx + 6, cy - 5);
        } else {
            g2.setColor(Theme.alpha(Color.WHITE, 65));
            g2.drawOval(checkX, checkY, checkSize, checkSize);
        }

        // 介绍
        g2.setFont(Theme.UI_SMALL);
        g2.setColor(new Color(0xC3C6D2));
        FontMetrics metrics = g2.getFontMetrics();
        int textWidth = width - 44;
        List<String> introLines = Theme.wrap(characterClass.intro(), metrics, textWidth, 2);
        int textY = 103;
        for (String line : introLines) {
            g2.drawString(line, 22, textY);
            textY += 20;
        }

        // 技能 chips
        int chipsBaseline = height - 40;
        int x = 22;
        int chipGap = 7;
        List<Skill> skills = characterClass.skills();
        if (!skills.isEmpty()) {
            g2.setFont(Theme.UI_SMALL);
            FontMetrics chipMetrics = g2.getFontMetrics();
            for (Skill skill : skills) {
                int chipWidth = chipMetrics.stringWidth(skill.name()) + 20;
                if (x + chipWidth > width - 22) {
                    break;
                }
                g2.setColor(Theme.alpha(accent, selected ? 26 : 14));
                g2.fillRoundRect(x, chipsBaseline - 17, chipWidth, 23, 23, 23);
                g2.setColor(Theme.alpha(accent, selected ? 110 : 60));
                g2.drawRoundRect(x, chipsBaseline - 17, chipWidth, 23, 23, 23);
                g2.setColor(selected ? Theme.TEXT : Theme.MUTED);
                g2.drawString(skill.name(), x + 10, chipsBaseline);
                x += chipWidth + chipGap;
            }
            g2.setColor(Theme.alpha(Color.WHITE, 24));
            g2.drawLine(22, height - 61, width - 22, height - 61);
        }

        if (hasFocus() && !selected) {
            g2.setColor(Theme.alpha(Theme.GOLD, 90));
            g2.drawRoundRect(3, 3, width - 7, height - 7, 15, 15);
        }

        g2.dispose();
    }
}
