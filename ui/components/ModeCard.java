package javawzgdyx.ui.components;

import javawzgdyx.game.GameMode;
import javawzgdyx.ui.Theme;

import javax.swing.JButton;
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

/** 主页的模式选择卡。 */
public class ModeCard extends JButton {

    private final GameMode mode;
    private final String emblem;
    private final String description;
    private final Color accent;

    public ModeCard(GameMode mode, String emblem, String description, Color accent) {
        super(mode.getTitle());
        this.mode = mode;
        this.emblem = emblem;
        this.description = description;
        this.accent = accent;
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(420, 248));
        setMaximumSize(new Dimension(440, 260));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        boolean hover = getModel().isRollover();
        boolean pressed = getModel().isPressed();

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(width * 0.86f, height * 0.06f),
                Math.max(width, height) * 0.75f,
                new float[]{0f, 1f},
                new Color[]{Theme.alpha(accent, hover ? 45 : 28), new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE
        ));
        g2.fillRoundRect(0, 0, width, height, 22, 22);

        Color top = hover ? new Color(0x202840) : new Color(0x181E32);
        Color bottom = hover ? new Color(0x0D1220) : new Color(0x0A0D18);
        if (pressed) {
            top = top.darker();
            bottom = bottom.darker();
        }
        g2.setPaint(new java.awt.GradientPaint(0, 0, top, 0, height, bottom));
        g2.fillRoundRect(0, 0, width, height, 22, 22);

        g2.setColor(Theme.alpha(accent, hover ? 150 : 60));
        g2.setStroke(new BasicStroke(hover ? 1.5f : 1f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 22, 22);

        // 顶部标签
        g2.setFont(Theme.tracked(Theme.MONO_BOLD, 0.18f));
        g2.setColor(Theme.alpha(accent, hover ? 235 : 180));
        g2.drawString(mode.getTag(), 28, 36);

        // 徽记
        int emblemSize = 54;
        int emblemX = width - 28 - emblemSize;
        int emblemY = 22;
        g2.setColor(Theme.alpha(accent, 22));
        g2.fillOval(emblemX, emblemY, emblemSize, emblemSize);
        g2.setColor(Theme.alpha(accent, 150));
        g2.drawOval(emblemX, emblemY, emblemSize, emblemSize);
        Theme.drawCentered(g2, emblem, new Font("KaiTi", Font.BOLD, 25), accent,
                emblemX + emblemSize / 2, emblemY + 36);

        // 标题
        g2.setFont(new Font("KaiTi", Font.BOLD, 30));
        g2.setColor(Theme.TEXT);
        g2.drawString(mode.getTitle(), 28, height - 100);

        // 描述
        g2.setFont(Theme.UI_SMALL);
        g2.setColor(Theme.MUTED);
        FontMetrics metrics = g2.getFontMetrics();
        List<String> lines = Theme.wrap(description, metrics, width - 56, 2);
        int lineY = height - 72;
        for (String line : lines) {
            g2.drawString(line, 28, lineY);
            lineY += 19;
        }

        // 底部入口
        g2.setFont(Theme.tracked(Theme.UI_BOLD_SMALL, 0.04f));
        g2.setColor(hover ? Theme.GOLD_SOFT : Theme.GOLD);
        int arrowOffset = hover ? 4 : 0;
        g2.drawString("进入战场  →", 28 + arrowOffset, height - 24);

        if (hasFocus()) {
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(Theme.alpha(Theme.GOLD, 100));
            g2.drawRoundRect(3, 3, width - 7, height - 7, 19, 19);
        }

        g2.dispose();
    }
}
