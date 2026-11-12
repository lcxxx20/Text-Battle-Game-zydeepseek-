package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** 无底色的文字按钮（退出游戏等次要操作）。 */
public class TextLinkButton extends JButton {

    public TextLinkButton(String text) {
        super(text);
        setFont(Theme.tracked(Theme.UI_SMALL, 0.05f));
        setForeground(Theme.MUTED);
        setBorder(new EmptyBorder(4, 10, 4, 10));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(getFont());
        return new Dimension(metrics.stringWidth(getText()) + 20, metrics.getHeight() + 8);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        g2.setFont(getFont());
        g2.setColor(getModel().isRollover() ? Theme.GOLD : getForeground());
        FontMetrics metrics = g2.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(getText())) / 2;
        int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(getText(), x, y);
        if (getModel().isRollover()) {
            g2.setColor(Theme.alpha(Theme.GOLD, 100));
            g2.drawLine(x, y + 3, x + metrics.stringWidth(getText()), y + 3);
        }
        g2.dispose();
    }
}
