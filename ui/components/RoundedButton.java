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

/** 圆角按钮：金色主按钮和低调的幽灵按钮。 */
public class RoundedButton extends JButton {

    public enum Style {
        PRIMARY, GHOST
    }

    private final Style style;
    private final int arc;

    public RoundedButton(String text, Style style) {
        this(text, style, 12);
    }

    public RoundedButton(String text, Style style, int arc) {
        super(text);
        this.style = style;
        this.arc = arc;
        setFont(Theme.tracked(Theme.UI_BOLD, 0.04f));
        setBorder(new EmptyBorder(0, 20, 0, 20));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width, 42));
    }

    public RoundedButton compact() {
        setFont(Theme.tracked(Theme.UI_SMALL, 0.03f));
        setBorder(new EmptyBorder(0, 14, 0, 14));
        setPreferredSize(new Dimension(getPreferredSize().width, 32));
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        boolean hover = getModel().isRollover() && isEnabled();
        boolean pressed = getModel().isPressed() && isEnabled();
        float alpha = isEnabled() ? 1f : 0.4f;

        if (style == Style.PRIMARY) {
            Color top = hover ? new Color(0xF7D99E) : new Color(0xEFC57F);
            Color bottom = hover ? new Color(0xD3A04C) : new Color(0xB9873A);
            if (pressed) {
                top = top.darker();
                bottom = bottom.darker();
            }
            g2.setPaint(new GradientPaint(0, 0, Theme.alpha(top, (int) (255 * alpha)),
                    0, height, Theme.alpha(bottom, (int) (255 * alpha))));
            g2.fillRoundRect(0, 0, width, height, arc, arc);
            g2.setColor(Theme.alpha(Theme.GOLD_SOFT, (int) (180 * alpha)));
            g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
            g2.setColor(Theme.alpha(new Color(0x2A1B07), (int) (230 * alpha)));
        } else {
            g2.setColor(Theme.alpha(Color.WHITE, hover ? 12 : 6));
            g2.fillRoundRect(0, 0, width, height, arc, arc);
            g2.setColor(Theme.alpha(hover ? Theme.GOLD : Color.WHITE, hover ? 110 : 36));
            g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
            g2.setColor(Theme.alpha(hover ? Theme.GOLD_SOFT : Theme.MUTED, (int) (255 * alpha)));
        }

        if (hasFocus() && isEnabled()) {
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(Theme.alpha(Theme.GOLD, 120));
            g2.drawRoundRect(2, 2, width - 5, height - 5, arc - 2, arc - 2);
        }

        Font font = getFont();
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int textX = (width - metrics.stringWidth(getText())) / 2;
        int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent() + (pressed ? 1 : 0);
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
