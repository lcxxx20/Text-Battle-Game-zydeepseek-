package javawzgdyx.ui.components;

import javawzgdyx.ui.Theme;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** 深色圆角输入框，带占位提示。 */
public class DarkTextField extends JTextField {

    private String placeholder;
    private int maxLength = 12;

    public DarkTextField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setBorder(new EmptyBorder(10, 14, 10, 14));
        setFont(Theme.UI.deriveFont(15f));
        setForeground(Theme.TEXT);
        setCaretColor(Theme.GOLD);
        setSelectionColor(new java.awt.Color(231, 185, 104, 70));
        setSelectedTextColor(Theme.TEXT);
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        setPreferredSize(new Dimension(220, 46));
        setMinimumSize(new Dimension(120, 42));
        installLimit();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    private void installLimit() {
        if (!(getDocument() instanceof AbstractDocument document)) {
            return;
        }
        document.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }
                int nextLength = fb.getDocument().getLength() + string.length();
                if (nextLength <= maxLength) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String value = text == null ? "" : text;
                int nextLength = fb.getDocument().getLength() - length + value.length();
                if (nextLength <= maxLength) {
                    super.replace(fb, offset, length, value, attrs);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.quality(g2);
        int width = getWidth();
        int height = getHeight();
        g2.setPaint(new java.awt.GradientPaint(0, 0,
                new java.awt.Color(0x121A2C), 0, height, new java.awt.Color(0x0A0F1A)));
        g2.fillRoundRect(0, 0, width, height, 12, 12);
        if (hasFocus()) {
            g2.setColor(Theme.alpha(Theme.GOLD, 22));
            g2.fillRoundRect(0, 0, width, height, 12, 12);
        }
        g2.setColor(new java.awt.Color(255, 255, 255, 12));
        g2.drawLine(14, 1, width - 14, 1);
        g2.setColor(hasFocus() ? Theme.alpha(Theme.GOLD, 180) : Theme.alpha(java.awt.Color.WHITE, 46));
        g2.setStroke(new BasicStroke(hasFocus() ? 1.4f : 1f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);
        g2.dispose();

        super.paintComponent(g);

        if (getText().isEmpty() && !hasFocus() && placeholder != null) {
            Graphics2D pg = (Graphics2D) g.create();
            Theme.quality(pg);
            pg.setFont(getFont());
            pg.setColor(Theme.alpha(Theme.FAINT, 220));
            FontMetrics metrics = pg.getFontMetrics();
            int x = getInsets().left;
            int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
            pg.drawString(placeholder, x, y);
            pg.dispose();
        }
    }
}
