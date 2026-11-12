package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** 窗口顶部的品牌栏。 */
public class BrandBar extends JPanel {

    public BrandBar() {
        setOpaque(false);
        setPreferredSize(new Dimension(10, 64));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();

        int sealSize = 36;
        int sealX = 24;
        int sealY = (height - sealSize) / 2;
        g2.setColor(Theme.alpha(Theme.GOLD, 16));
        g2.fillRoundRect(sealX, sealY, sealSize, sealSize, 11, 11);
        g2.setColor(Theme.alpha(Theme.GOLD, 170));
        g2.drawRoundRect(sealX, sealY, sealSize, sealSize, 11, 11);
        Theme.drawCentered(g2, "斗", new Font("KaiTi", Font.BOLD, 22), Theme.GOLD,
                sealX + sealSize / 2, sealY + 26);

        g2.setFont(Theme.tracked(new Font("KaiTi", Font.BOLD, 20), 0.08f));
        g2.setColor(Theme.TEXT);
        g2.drawString("文字格斗", sealX + sealSize + 14, sealY + 20);

        g2.setFont(Theme.tracked(Theme.MONO, 0.22f));
        g2.setColor(Theme.FAINT);
        g2.drawString("WENZI FIGHT", sealX + sealSize + 15, sealY + 34);

        String edition = "JAVA EDITION";
        Font editionFont = Theme.tracked(Theme.MONO, 0.28f);
        g2.setFont(editionFont);
        FontMetrics metrics = g2.getFontMetrics();
        int editionWidth = metrics.stringWidth(edition);
        int right = width - 28;
        g2.setColor(Theme.alpha(Theme.GOLD, 190));
        g2.drawString(edition, right - editionWidth, height / 2 + 4);

        int lineEnd = right - editionWidth - 16;
        int lineStart = Math.max(sealX + sealSize + 180, lineEnd - 130);
        if (lineEnd > lineStart) {
            g2.setPaint(new GradientPaint(lineStart, 0, new Color(255, 255, 255, 0),
                    lineEnd, 0, new Color(255, 255, 255, 42)));
            g2.drawLine(lineStart, height / 2, lineEnd, height / 2);
        }

        g2.setPaint(new GradientPaint(0, height - 1, new Color(255, 255, 255, 0),
                width / 2f, height - 1, new Color(255, 255, 255, 30)));
        g2.drawLine(0, height - 1, width / 2, height - 1);
        g2.setPaint(new GradientPaint(width / 2f, height - 1, new Color(255, 255, 255, 30),
                width, height - 1, new Color(255, 255, 255, 0)));
        g2.drawLine(width / 2, height - 1, width, height - 1);

        g2.dispose();
    }
}
