package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** 选角步骤指示器。 */
public class StepPips extends JComponent {

    private String[] labels = new String[0];
    private int current;

    public StepPips() {
        setOpaque(false);
        setPreferredSize(new Dimension(160, 30));
    }

    public void setSteps(String[] labels, int current) {
        this.labels = labels == null ? new String[0] : labels.clone();
        this.current = current;
        setPreferredSize(new Dimension(computeWidth(), 30));
        revalidate();
        repaint();
    }

    private Font font() {
        return Theme.tracked(Theme.UI_TINY, 0.04f);
    }

    private int computeWidth() {
        FontMetrics metrics = getFontMetrics(font());
        int width = 0;
        for (String label : labels) {
            width += metrics.stringWidth(label) + 22;
        }
        width += Math.max(0, labels.length - 1) * 10;
        return Math.max(10, width);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        Font font = font();
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int x = 0;
        int height = 26;
        int y = (getHeight() - height) / 2;

        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            int width = metrics.stringWidth(label) + 22;
            Color fg;
            Color bg;
            Color border;
            if (i == current) {
                fg = Theme.GOLD_SOFT;
                bg = Theme.alpha(Theme.GOLD, 22);
                border = Theme.alpha(Theme.GOLD, 140);
            } else if (i < current) {
                fg = Theme.GREEN;
                bg = Theme.alpha(Theme.GREEN, 10);
                border = Theme.alpha(Theme.GREEN, 70);
            } else {
                fg = Theme.FAINT;
                bg = new Color(255, 255, 255, 5);
                border = Theme.alpha(Color.WHITE, 24);
            }
            g2.setColor(bg);
            g2.fillRoundRect(x, y, width, height, height, height);
            g2.setColor(border);
            g2.drawRoundRect(x, y, width, height, height, height);
            g2.setColor(fg);
            g2.drawString(label, x + 11, y + (height - metrics.getHeight()) / 2 + metrics.getAscent());
            x += width;
            if (i < labels.length - 1) {
                g2.setColor(Theme.alpha(Color.WHITE, 28));
                g2.drawLine(x + 3, y + height / 2, x + 7, y + height / 2);
                x += 10;
            }
        }
        g2.dispose();
    }
}
