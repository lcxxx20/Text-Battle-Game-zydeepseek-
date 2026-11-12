package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

/** 战斗行动区里的技能卡 / 出手选择卡。 */
public class SkillCardButton extends JButton {

    private final String subtitle;
    private final String tag;
    private final Color tone;

    public SkillCardButton(String title, String subtitle, String tag) {
        this(title, subtitle, tag, toneFor(tag));
    }

    public SkillCardButton(String title, String subtitle, String tag, Color tone) {
        super(title);
        this.subtitle = subtitle;
        this.tag = tag;
        this.tone = tone;
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(240, 96));
        setMinimumSize(new Dimension(140, 86));
        setToolTipText(title + "：" + subtitle);
    }

    private static Color toneFor(String tag) {
        if (tag == null) {
            return Theme.GOLD;
        }
        return switch (tag) {
            case "辅助" -> Theme.CYAN;
            case "爆发" -> Theme.VIOLET;
            case "重击" -> Theme.AMBER;
            case "回复" -> Theme.GREEN;
            case "混沌" -> Theme.PINK;
            case "禁忌" -> Theme.GOLD;
            case "出手" -> Theme.GOLD;
            default -> Theme.GOLD;
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        boolean hover = getModel().isRollover() && isEnabled();
        boolean pressed = getModel().isPressed() && isEnabled();
        float alpha = isEnabled() ? 1f : 0.42f;

        g2.setPaint(new GradientPaint(0, 0,
                Theme.alpha(hover ? new Color(0x222A42) : new Color(0x161D30), (int) (255 * alpha)),
                0, height,
                Theme.alpha(new Color(0x0B0F1B), (int) (255 * alpha))));
        g2.fillRoundRect(0, 0, width, height, 14, 14);

        if (hover) {
            g2.setColor(Theme.alpha(tone, 26));
            g2.fillRoundRect(0, 0, width, height, 14, 14);
        }

        g2.setColor(Theme.alpha(hover ? tone : Color.WHITE, hover ? 120 : 32));
        g2.setStroke(new BasicStroke(hover ? 1.3f : 1f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 14, 14);
        g2.setColor(Theme.alpha(tone, hover ? 120 : 55));
        g2.fillRoundRect(0, 14, 3, height - 28, 3, 3);

        // 标题
        Font titleFont = Theme.tracked(Theme.UI_BOLD, 0.02f);
        g2.setFont(titleFont);
        FontMetrics titleMetrics = g2.getFontMetrics();

        Font tagFont = Theme.tracked(Theme.UI_TINY.deriveFont(Font.BOLD, 10f), 0.08f);
        FontMetrics tagMetrics = getFontMetrics(tagFont);
        int tagWidth = tagMetrics.stringWidth(tag) + 16;
        int tagHeight = tagMetrics.getHeight() + 2;

        int titleMaxWidth = width - 32 - tagWidth - 8;
        String title = getText();
        while (!title.isEmpty() && titleMetrics.stringWidth(title) > titleMaxWidth) {
            title = title.substring(0, title.length() - 1);
        }
        if (!title.equals(getText())) {
            title = title.length() > 1 ? title.substring(0, title.length() - 1) + "…" : title;
        }
        g2.setColor(Theme.alpha(Theme.TEXT, (int) (255 * alpha)));
        g2.drawString(title, 16, 26);

        // 标签
        int tagX = width - 14 - tagWidth;
        int tagY = 13;
        g2.setColor(Theme.alpha(tone, (int) (22 * alpha)));
        g2.fillRoundRect(tagX, tagY, tagWidth, tagHeight, tagHeight, tagHeight);
        g2.setColor(Theme.alpha(tone, (int) (100 * alpha)));
        g2.drawRoundRect(tagX, tagY, tagWidth, tagHeight, tagHeight, tagHeight);
        g2.setFont(tagFont);
        g2.setColor(Theme.alpha(tone, (int) (235 * alpha)));
        g2.drawString(tag, tagX + 8, tagY + 3 + tagMetrics.getAscent());

        // 描述
        g2.setFont(Theme.UI_TINY.deriveFont(11.5f));
        g2.setColor(Theme.alpha(Theme.MUTED, (int) (255 * alpha)));
        FontMetrics descMetrics = g2.getFontMetrics();
        List<String> lines = Theme.wrap(subtitle, descMetrics, width - 32, 2);
        int y = 48;
        for (String line : lines) {
            g2.drawString(line, 16, y);
            y += 16;
        }

        if (hasFocus() && isEnabled()) {
            g2.setColor(Theme.alpha(Theme.GOLD, 100));
            g2.drawRoundRect(2, 2, width - 5, height - 5, 12, 12);
        }

        g2.dispose();
    }
}
