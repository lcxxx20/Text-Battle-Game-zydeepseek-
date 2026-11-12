package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

/** 整个窗口共用的深夜竞技场背景。 */
public class BackgroundPanel extends JPanel {

    public BackgroundPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            g2.dispose();
            return;
        }

        g2.setPaint(new GradientPaint(0, 0, Theme.BG_TOP, 0, height, Theme.BG_BOTTOM));
        g2.fillRect(0, 0, width, height);

        glow(g2, width * 0.12, height * 0.04, width * 0.55, Theme.alpha(Theme.GOLD, 26));
        glow(g2, width * 0.88, height * 0.18, width * 0.55, Theme.alpha(Theme.RED_DEEP, 30));
        glow(g2, width * 0.45, height * 0.95, width * 0.5, Theme.alpha(Theme.CYAN, 12));

        g2.setColor(new Color(255, 255, 255, 7));
        int step = 56;
        for (int x = 0; x < width; x += step) {
            g2.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += step) {
            g2.drawLine(0, y, width, y);
        }

        float centerX = width / 2f;
        float centerY = height * 0.42f;
        float radius = Math.max(width, height) * 0.78f;
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Float(centerX, centerY),
                radius,
                new float[]{0f, 0.55f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 36), new Color(0, 0, 0, 150)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, width, height);

        g2.dispose();
    }

    private void glow(Graphics2D g2, double centerX, double centerY, double radius, Color color) {
        if (radius <= 1) {
            return;
        }
        Color transparent = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        g2.setPaint(new RadialGradientPaint(
                new Point2D.Double(centerX, centerY),
                (float) radius,
                new float[]{0f, 1f},
                new Color[]{color, transparent},
                MultipleGradientPaint.CycleMethod.NO_CYCLE
        ));
        g2.fillOval((int) (centerX - radius), (int) (centerY - radius),
                (int) (radius * 2), (int) (radius * 2));
    }
}
