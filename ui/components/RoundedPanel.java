package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

/** 带渐变色与圆角描边的容器。 */
public class RoundedPanel extends JPanel {

    private Color top;
    private Color bottom;
    private Color border;
    private int arc;

    public RoundedPanel(LayoutManager layout) {
        this(layout, Theme.PANEL_TOP, Theme.PANEL_BOTTOM, Theme.LINE, 18);
    }

    public RoundedPanel(LayoutManager layout, Color top, Color bottom, Color border, int arc) {
        super(layout);
        this.top = top;
        this.bottom = bottom;
        this.border = border;
        this.arc = arc;
        setOpaque(false);
    }

    public void setPalette(Color top, Color bottom, Color border) {
        this.top = top;
        this.bottom = bottom;
        this.border = border;
        repaint();
    }

    public void setBorderColor(Color border) {
        this.border = border;
        repaint();
    }

    public void setArc(int arc) {
        this.arc = arc;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        Theme.fillRounded(g2, 0, 0, width, height, arc, top, bottom);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
        // 顶部高光
        g2.setColor(new Color(255, 255, 255, 10));
        g2.drawLine(arc / 2, 1, width - arc / 2, 1);
        g2.dispose();
    }
}
