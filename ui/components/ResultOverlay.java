package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * 铺满窗口的结算遮罩（作为 JFrame 的 glassPane 使用）。
 * 半透明压暗背景 + 居中的胜负卡片。
 */
public class ResultOverlay extends JPanel {

    private final RoundedPanel card = new RoundedPanel(null);
    private final Glyph glyph = new Glyph();
    private final JLabel kicker = new JLabel("VICTORY");
    private final JLabel title = new JLabel("胜利");
    private final WrapLabel message = new WrapLabel();
    private final JPanel buttons = new JPanel();

    public ResultOverlay() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 40, 28, 40));
        card.setPalette(new Color(0x161C2D), new Color(0x080B14), Theme.alpha(Theme.GOLD, 110));

        glyph.setAlignmentX(CENTER_ALIGNMENT);
        card.add(glyph);
        card.add(Box.createVerticalStrut(16));

        kicker.setFont(Theme.tracked(Theme.MONO_BOLD, 0.28f));
        kicker.setForeground(Theme.GOLD);
        kicker.setAlignmentX(CENTER_ALIGNMENT);
        card.add(kicker);
        card.add(Box.createVerticalStrut(6));

        title.setFont(new Font("KaiTi", Font.BOLD, 36));
        title.setForeground(Theme.TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        message.setAlignmentX(CENTER_ALIGNMENT);
        card.add(message);
        card.add(Box.createVerticalStrut(22));

        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setAlignmentX(CENTER_ALIGNMENT);
        card.add(buttons);

        add(card);
        setVisible(false);
    }

    public void showResult(boolean victory, String messageText, Runnable onRematch, Runnable onMenu) {
        Color accent = victory ? Theme.GOLD : Theme.RED;
        card.setPalette(new Color(0x161C2D), new Color(0x080B14), Theme.alpha(accent, 120));
        glyph.setGlyph(victory ? "胜" : "败", accent);
        kicker.setText(victory ? "VICTORY" : "DEFEAT");
        kicker.setForeground(accent);
        title.setText(victory ? "胜利" : "败北");
        title.setForeground(Theme.TEXT);
        message.setText(messageText, accent);

        buttons.removeAll();
        RoundedButton rematch = new RoundedButton("再战一局", RoundedButton.Style.PRIMARY);
        rematch.setAlignmentX(CENTER_ALIGNMENT);
        rematch.setMaximumSize(new Dimension(320, 46));
        rematch.setPreferredSize(new Dimension(300, 46));
        rematch.addActionListener(event -> {
            setVisible(false);
            onRematch.run();
        });
        buttons.add(rematch);
        buttons.add(Box.createVerticalStrut(9));

        RoundedButton menu = new RoundedButton("返回主菜单", RoundedButton.Style.GHOST);
        menu.setAlignmentX(CENTER_ALIGNMENT);
        menu.setMaximumSize(new Dimension(320, 44));
        menu.setPreferredSize(new Dimension(300, 44));
        menu.addActionListener(event -> {
            setVisible(false);
            onMenu.run();
        });
        buttons.add(menu);

        revalidate();
        repaint();
        SwingUtilities.invokeLater(rematch::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(3, 5, 10, 200));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(231, 185, 104, 9),
                0, getHeight(), new Color(0, 0, 0, 0)));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    /** 胜负大字徽记。 */
    private static class Glyph extends JComponent {
        private String text = "胜";
        private Color accent = Theme.GOLD;

        Glyph() {
            setOpaque(false);
            setPreferredSize(new Dimension(86, 86));
            setMinimumSize(new Dimension(86, 86));
            setMaximumSize(new Dimension(86, 86));
        }

        void setGlyph(String text, Color accent) {
            this.text = text;
            this.accent = accent;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.quality(g2);
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setPaint(new GradientPaint(0, y, Theme.alpha(accent, 30),
                    0, y + size, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0)));
            g2.fillOval(x, y, size, size);
            g2.setColor(Theme.alpha(accent, 55));
            g2.setStroke(new BasicStroke(3f));
            g2.drawOval(x + 2, y + 2, size - 5, size - 5);
            g2.setColor(Theme.alpha(accent, 170));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(x + 7, y + 7, size - 15, size - 15);

            g2.setFont(new Font("KaiTi", Font.BOLD, 42));
            g2.setColor(accent);
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(text, x + (size - metrics.stringWidth(text)) / 2,
                    y + (size - metrics.getHeight()) / 2 + metrics.getAscent());
            g2.dispose();
        }
    }

    /** 居中、自动换行的说明文字。 */
    private static class WrapLabel extends JComponent {
        private String text = "";
        private Color accent = Theme.GOLD;
        private final Font font = Theme.UI_SMALL.deriveFont(13f);
        private static final int WIDTH = 370;
        private static final int LINE_HEIGHT = 21;

        WrapLabel() {
            setOpaque(false);
            setAlignmentX(CENTER_ALIGNMENT);
            updateSize();
        }

        void setText(String text, Color accent) {
            this.text = text == null ? "" : text;
            this.accent = accent;
            updateSize();
            repaint();
        }

        private void updateSize() {
            FontMetrics metrics = getFontMetrics(font);
            List<String> lines = Theme.wrap(text, metrics, WIDTH);
            setPreferredSize(new Dimension(WIDTH, Math.max(LINE_HEIGHT, lines.size() * LINE_HEIGHT)));
            setMinimumSize(getPreferredSize());
            setMaximumSize(new Dimension(WIDTH, 400));
            revalidate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.quality(g2);
            g2.setFont(font);
            g2.setColor(Theme.MUTED);
            FontMetrics metrics = g2.getFontMetrics();
            List<String> lines = Theme.wrap(text, metrics, getWidth());
            int y = (getHeight() - lines.size() * LINE_HEIGHT) / 2 + metrics.getAscent();
            for (String line : lines) {
                int x = (getWidth() - metrics.stringWidth(line)) / 2;
                g2.drawString(line, x, y);
                y += LINE_HEIGHT;
            }
            g2.setColor(Theme.alpha(accent, 90));
            g2.fillRoundRect(getWidth() / 2 - 18, getHeight() - 1, 36, 2, 2, 2);
            g2.dispose();
        }
    }
}
