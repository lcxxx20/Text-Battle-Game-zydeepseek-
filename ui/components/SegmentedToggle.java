package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** 性别选择按钮。 */
public class SegmentedToggle extends JToggleButton {

    public SegmentedToggle(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(0, 18, 0, 18));
        setFont(Theme.tracked(Theme.UI_SMALL, 0.04f));
        setPreferredSize(new Dimension(82, 36));
        setRolloverEnabled(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        boolean selected = isSelected();
        boolean hover = getModel().isRollover();

        g2.setColor(selected ? Theme.alpha(Theme.GOLD, 28) : new Color(255, 255, 255, hover ? 9 : 5));
        g2.fillRoundRect(0, 0, width, height, 10, 10);
        g2.setColor(selected ? Theme.alpha(Theme.GOLD, 160)
                : Theme.alpha(Color.WHITE, hover ? 55 : 26));
        g2.setStroke(new BasicStroke(selected ? 1.3f : 1f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);

        g2.setFont(getFont());
        g2.setColor(selected ? Theme.GOLD_SOFT : Theme.MUTED);
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(getText(), (width - metrics.stringWidth(getText())) / 2,
                (height - metrics.getHeight()) / 2 + metrics.getAscent());
        g2.dispose();
    }
}
