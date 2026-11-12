package javawzgdyx.ui.components;

import javawzgdyx.game.LogTone;
import javawzgdyx.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

/** 战斗日志：深色滚动文本区、彩色事件与淡淡的「战」字水印。 */
public class LogPane extends JPanel {

    private final JTextPane textPane = new JTextPane();
    private final JScrollPane scrollPane;
    private final LiveDot liveDot = new LiveDot();

    public LogPane() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(liveDot);
        JLabel title = new JLabel("战斗记录");
        title.setFont(Theme.tracked(Theme.UI_BOLD_SMALL, 0.06f));
        title.setForeground(Theme.GOLD_SOFT);
        left.add(title);
        header.add(left, BorderLayout.WEST);

        JLabel live = new JLabel("LIVE");
        live.setFont(Theme.tracked(Theme.MONO, 0.24f));
        live.setForeground(Theme.alpha(Theme.GREEN, 170));
        header.add(live, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        textPane.setFont(Theme.UI_SMALL);
        textPane.setForeground(Theme.TEXT);
        textPane.setCaretColor(Theme.GOLD);
        textPane.setSelectionColor(new Color(231, 185, 104, 55));
        textPane.setSelectedTextColor(Theme.TEXT);
        textPane.setFocusable(false);

        scrollPane = new JScrollPane(textPane);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        liveDot.start();
    }

    @Override
    public void removeNotify() {
        liveDot.stop();
        super.removeNotify();
    }

    public void append(String text, LogTone tone) {
        StyledDocument document = textPane.getStyledDocument();
        try {
            if (document.getLength() > 0 && (tone == LogTone.TURN || tone == LogTone.SYSTEM)) {
                document.insertString(document.getLength(), "\n", null);
            }
            if (document.getLength() > 0) {
                document.insertString(document.getLength(), "\n", null);
            }
            Style style = document.addStyle("tone", null);
            StyleConstants.setForeground(style, colorFor(tone));
            StyleConstants.setFontFamily(style, Theme.UI.getFamily());
            StyleConstants.setFontSize(style, tone == LogTone.TURN ? 13 : 12);
            StyleConstants.setBold(style, tone == LogTone.TURN || tone == LogTone.GOOD);
            document.insertString(document.getLength(), prefix(tone) + text, style);
        } catch (Exception ignored) {
            // 文本插入不应失败，兜底避免影响战斗流程。
        }
        textPane.setCaretPosition(document.getLength());
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    public void clear() {
        textPane.setText("");
    }

    private static String prefix(LogTone tone) {
        return switch (tone) {
            case TURN -> "◆ ";
            case SYSTEM -> "◇ ";
            case GOOD -> "★ ";
            case WARN -> "⚠ ";
            default -> "› ";
        };
    }

    private static Color colorFor(LogTone tone) {
        return switch (tone) {
            case SYSTEM -> Theme.FAINT;
            case TURN -> Theme.GOLD;
            case PLAYER -> Theme.CYAN;
            case ENEMY -> Theme.RED;
            case SKILL -> Theme.VIOLET;
            case HEAL -> Theme.GREEN;
            case GOOD -> Theme.GOLD_SOFT;
            case WARN -> Theme.AMBER;
            case MUTED -> Theme.MUTED;
            case WEIRD -> Theme.PINK;
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        Theme.fillRounded(g2, 0, 0, width, height, 18, Theme.PANEL_TOP, Theme.PANEL_BOTTOM);
        g2.setColor(Theme.LINE);
        g2.drawRoundRect(0, 0, width - 1, height - 1, 18, 18);
        g2.setColor(new Color(255, 255, 255, 10));
        g2.drawLine(9, 1, width - 9, 1);

        g2.setFont(new java.awt.Font("KaiTi", java.awt.Font.BOLD, 170));
        g2.setColor(new Color(231, 185, 104, 12));
        g2.drawString("战", width - 150, height - 10);
        g2.dispose();
    }

    /** 标题旁缓慢呼吸的小绿点。 */
    private static final class LiveDot extends JComponent {
        private float phase;
        private javax.swing.Timer timer;

        LiveDot() {
            setPreferredSize(new Dimension(9, 9));
            setMinimumSize(new Dimension(9, 9));
        }

        void start() {
            if (timer == null) {
                timer = new javax.swing.Timer(90, event -> {
                    phase += 0.12f;
                    repaint();
                });
            }
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        void stop() {
            if (timer != null) {
                timer.stop();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.quality(g2);
            int alpha = 130 + (int) (80 * (Math.sin(phase) + 1) / 2);
            g2.setColor(Theme.alpha(Theme.GREEN, 45));
            g2.fillOval(0, 0, 9, 9);
            g2.setColor(Theme.alpha(Theme.GREEN, alpha));
            g2.fillOval(2, 2, 5, 5);
            g2.dispose();
        }
    }

    /** 细窄的深色滚动条。 */
    private static final class DarkScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            trackColor = new Color(0, 0, 0, 0);
            thumbColor = new Color(255, 255, 255, 45);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private JButton zeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            button.setBorder(BorderFactory.createEmptyBorder());
            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle trackBounds) {
            // 透明轨道
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.quality(g2);
            g2.setColor(isThumbRollover() ? new Color(231, 185, 104, 110) : new Color(255, 255, 255, 48));
            int arc = 8;
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 2,
                    thumbBounds.width - 2, thumbBounds.height - 4, arc, arc);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(8, super.getPreferredSize(c).height);
        }
    }
}
