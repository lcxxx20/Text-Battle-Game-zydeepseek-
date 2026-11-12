package javawzgdyx.ui;

import javawzgdyx.model.CharacterClass;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局配色、字体和绘制工具。视觉方向与网页版保持一致：
 * 深夜竞技场、暖金主色、朱红与青绿作为阵营色。
 */
public final class Theme {

    public static final Color BG_TOP = new Color(0x080B14);
    public static final Color BG_BOTTOM = new Color(0x060810);
    public static final Color PANEL_TOP = new Color(0x1A2035);
    public static final Color PANEL_BOTTOM = new Color(0x0B0F1B);
    public static final Color PANEL = new Color(0x101624);
    public static final Color TEXT = new Color(0xECE9E2);
    public static final Color MUTED = new Color(0x8B90A3);
    public static final Color FAINT = new Color(0x5F6578);
    public static final Color GOLD = new Color(0xE7B968);
    public static final Color GOLD_SOFT = new Color(0xF3D49C);
    public static final Color GOLD_DEEP = new Color(0xA97A2E);
    public static final Color RED = new Color(0xFF5B6B);
    public static final Color RED_DEEP = new Color(0xB32636);
    public static final Color GREEN = new Color(0x6FD68A);
    public static final Color CYAN = new Color(0x63D6C9);
    public static final Color VIOLET = new Color(0xA78BFA);
    public static final Color AMBER = new Color(0xF0A24B);
    public static final Color PINK = new Color(0xF27BA9);

    public static final Color LINE = new Color(255, 255, 255, 24);
    public static final Color LINE_STRONG = new Color(255, 255, 255, 46);

    public static final Font UI = new Font("Microsoft YaHei", Font.PLAIN, 14);
    public static final Font UI_SMALL = UI.deriveFont(Font.PLAIN, 12f);
    public static final Font UI_TINY = UI.deriveFont(Font.PLAIN, 10f);
    public static final Font UI_BOLD = UI.deriveFont(Font.BOLD, 14f);
    public static final Font UI_BOLD_SMALL = UI.deriveFont(Font.BOLD, 12f);
    public static final Font DISPLAY = new Font("KaiTi", Font.BOLD, 26);
    public static final Font DISPLAY_LARGE = new Font("KaiTi", Font.BOLD, 84);
    public static final Font MONO = new Font("Consolas", Font.PLAIN, 11);
    public static final Font MONO_BOLD = new Font("Consolas", Font.BOLD, 11);

    private Theme() {
    }

    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // 使用默认外观即可，界面绝大部分是自绘的。
        }
        UIManager.put("Label.font", UI);
        UIManager.put("Button.font", UI);
        UIManager.put("TextField.font", UI);
        UIManager.put("OptionPane.messageFont", UI);
        UIManager.put("OptionPane.buttonFont", UI);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("Panel.background", PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT);
    }

    public static void quality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color mix(Color a, Color b, float t) {
        float clamped = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * clamped),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * clamped),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * clamped),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * clamped)
        );
    }

    public static Font tracked(Font base, float tracking) {
        Map<TextAttribute, Object> attributes = new HashMap<>(base.getAttributes());
        attributes.put(TextAttribute.TRACKING, tracking);
        return base.deriveFont(attributes);
    }

    public static Color accentFor(CharacterClass characterClass) {
        if (characterClass == null) {
            return GOLD;
        }
        return switch (characterClass) {
            case BUSIZHE -> VIOLET;
            case LIJIACHENG -> AMBER;
            case ZAYU -> RED;
        };
    }

    public static Color hpColor(boolean enemySide, double ratio) {
        if (enemySide) {
            return RED;
        }
        if (ratio <= 0.25) {
            return RED;
        }
        if (ratio <= 0.5) {
            return AMBER;
        }
        return GREEN;
    }

    public static void fillRounded(Graphics2D g2, int x, int y, int width, int height,
                                   int arc, Color top, Color bottom) {
        if (width <= 0 || height <= 0) {
            return;
        }
        g2.setPaint(new GradientPaint(x, y, top, x, y + height, bottom));
        g2.fillRoundRect(x, y, width, height, arc, arc);
    }

    public static void drawCentered(Graphics2D g2, String text, Font font, Color color,
                                    int centerX, int baselineY) {
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    /** 这些标点不单独出现在行首，宁可让上一行略微超宽。 */
    private static final String NO_LINE_START = "，。！？；：、）》」』】〕…—·,.!?;:)]}%";

    public static List<String> wrap(String text, FontMetrics metrics, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                lines.add(current.toString());
                current.setLength(0);
                continue;
            }
            String candidate = current.toString() + ch;
            boolean closingPunctuation = NO_LINE_START.indexOf(ch) >= 0;
            if (current.length() > 0 && !closingPunctuation
                    && metrics.stringWidth(candidate) > maxWidth) {
                lines.add(current.toString());
                current.setLength(0);
            }
            current.append(ch);
        }
        if (current.length() > 0 || lines.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    public static List<String> wrap(String text, FontMetrics metrics, int maxWidth, int maxLines) {
        List<String> all = wrap(text, metrics, maxWidth);
        if (all.size() <= maxLines) {
            return all;
        }
        List<String> shown = new ArrayList<>(all.subList(0, maxLines));
        String last = shown.get(maxLines - 1);
        while (!last.isEmpty() && metrics.stringWidth(last + "…") > maxWidth) {
            last = last.substring(0, last.length() - 1);
        }
        shown.set(maxLines - 1, last + "…");
        return shown;
    }

    /** 画一个胶囊形标签。 */
    public static void drawPill(Graphics2D g2, String text, Font font, int x, int baselineY,
                                Color textColor, Color fillColor, Color borderColor) {
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int height = metrics.getHeight() + 2;
        int width = textWidth + 18;
        int y = baselineY - metrics.getAscent() - 4;
        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, width, height, height, height);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(x, y, width, height, height, height);
        }
        g2.setColor(textColor);
        g2.drawString(text, x + 9, y + 3 + metrics.getAscent());
    }

    public static Image appIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        quality(g2);
        g2.setColor(new Color(0x0B0F1B));
        g2.fillRoundRect(2, 2, 60, 60, 16, 16);
        g2.setColor(GOLD);
        g2.setStroke(new java.awt.BasicStroke(1.6f));
        g2.drawRoundRect(3, 3, 58, 58, 16, 16);
        g2.setFont(new Font("KaiTi", Font.BOLD, 34));
        drawCentered(g2, "斗", new Font("KaiTi", Font.BOLD, 34), GOLD, 32, 45);
        g2.dispose();
        return image;
    }
}
