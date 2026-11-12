package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** 主页的大标题，逐字绘制，带金色渐变与淡淡的光晕。 */
public class TitleArt extends JComponent {

    private static final String TITLE = "文字格斗";

    public TitleArt() {
        setOpaque(false);
        setPreferredSize(new Dimension(920, 128));
        setMaximumSize(new Dimension(920, 128));
        setMinimumSize(new Dimension(500, 100));
        setAlignmentX(CENTER_ALIGNMENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        Font font = Theme.DISPLAY_LARGE;
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();

        float tracking = 16f;
        float total = 0;
        for (int i = 0; i < TITLE.length(); i++) {
            total += metrics.charWidth(TITLE.charAt(i));
        }
        total += tracking * (TITLE.length() - 1);
        float x = (getWidth() - total) / 2f;
        float baseline = getHeight() / 2f + metrics.getAscent() / 2f - 6;

        // 柔和的外发光
        for (int i = 3; i >= 1; i--) {
            g2.setColor(new Color(231, 185, 104, 8 * i));
            drawTracked(g2, font, TITLE, x - i * 0.8f, baseline - i * 0.8f, tracking, total);
        }

        g2.setPaint(new GradientPaint(0, baseline - 82, new Color(0xFDF7E8),
                0, baseline, new Color(0xB6873A)));
        drawTracked(g2, font, TITLE, x, baseline, tracking, total);

        g2.setColor(new Color(255, 255, 255, 32));
        g2.fillRoundRect((int) (getWidth() / 2f - 52), (int) baseline + 18, 104, 2, 2, 2);
        g2.setColor(Theme.alpha(Theme.GOLD, 90));
        g2.fillRoundRect((int) (getWidth() / 2f - 28), (int) baseline + 18, 56, 2, 2, 2);

        g2.dispose();
    }

    private void drawTracked(Graphics2D g2, Font font, String text, float x, float baseline,
                             float tracking, float total) {
        FontMetrics metrics = g2.getFontMetrics(font);
        float cursor = x;
        for (int i = 0; i < text.length(); i++) {
            String piece = String.valueOf(text.charAt(i));
            g2.drawString(piece, cursor, baseline);
            cursor += metrics.stringWidth(piece) + tracking;
        }
    }
}
