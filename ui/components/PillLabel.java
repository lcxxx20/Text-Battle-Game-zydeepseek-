package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** 小胶囊标签，用于战斗顶部的模式徽章。 */
public class PillLabel extends JComponent {

    private String text;
    private Font font;
    private Color foreground;
    private Color background;
    private Color border;
    private int horizontalPadding = 12;

    public PillLabel(String text) {
        this.text = text;
        this.font = Theme.tracked(Theme.MONO_BOLD, 0.18f);
        this.foreground = Theme.GOLD;
        this.background = Theme.alpha(Theme.GOLD, 14);
        this.border = Theme.alpha(Theme.GOLD, 80);
        setOpaque(false);
    }

    public void setText(String text) {
        this.text = text;
        revalidate();
        repaint();
    }

    public void setColors(Color foreground, Color background, Color border) {
        this.foreground = foreground;
        this.background = background;
        this.border = border;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(font);
        int width = metrics.stringWidth(text) + horizontalPadding * 2;
        int height = metrics.getHeight() + 8;
        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        g2.setColor(background);
        g2.fillRoundRect(0, 0, width, height, height, height);
        g2.setColor(border);
        g2.drawRoundRect(0, 0, width - 1, height - 1, height, height);
        g2.setFont(font);
        g2.setColor(foreground);
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, (width - metrics.stringWidth(text)) / 2,
                (height - metrics.getHeight()) / 2 + metrics.getAscent());
        g2.dispose();
    }
}
