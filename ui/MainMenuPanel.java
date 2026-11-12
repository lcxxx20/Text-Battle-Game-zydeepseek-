package javawzgdyx.ui;

import javawzgdyx.game.GameMode;
import javawzgdyx.model.CharacterClass;
import javawzgdyx.ui.components.ModeCard;
import javawzgdyx.ui.components.TextLinkButton;
import javawzgdyx.ui.components.TitleArt;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.List;

/** 开始界面：标题、两种模式与角色一览。 */
public class MainMenuPanel extends JPanel {

    public interface Listener {
        void onMode(GameMode mode);

        void onExit();
    }

    public MainMenuPanel(Listener listener) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(16, 28, 16, 28));

        add(Box.createVerticalGlue());

        JLabel eyebrow = new JLabel("C O N S O L E   ·   R E B O R N");
        eyebrow.setFont(Theme.tracked(Theme.MONO, 0.3f));
        eyebrow.setForeground(Theme.alpha(Theme.GOLD, 215));
        eyebrow.setAlignmentX(CENTER_ALIGNMENT);
        add(eyebrow);

        add(Box.createVerticalStrut(6));
        TitleArt title = new TitleArt();
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);

        JLabel subtitle = new JLabel("在回合与运气之间，打出属于你的文字格斗。");
        subtitle.setFont(Theme.UI);
        subtitle.setForeground(Theme.MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        add(subtitle);

        add(Box.createVerticalStrut(26));

        JPanel modeRow = new JPanel(new GridLayout(1, 2, 18, 0));
        modeRow.setOpaque(false);
        modeRow.setAlignmentX(CENTER_ALIGNMENT);
        modeRow.setPreferredSize(new Dimension(900, 250));
        modeRow.setMaximumSize(new Dimension(900, 260));
        modeRow.setMinimumSize(new Dimension(700, 220));

        ModeCard solo = new ModeCard(GameMode.SOLO, "D",
                "迎战杂鱼 deepseek。你有 20 个回合，在回合耗尽前把它打倒。",
                Theme.GOLD);
        solo.addActionListener(event -> listener.onMode(GameMode.SOLO));

        ModeCard duel = new ModeCard(GameMode.DUEL, "VS",
                "同屏双人对战，自由选择出手顺序。没有回合限制，直到一方倒下。",
                Theme.RED);
        duel.addActionListener(event -> listener.onMode(GameMode.DUEL));

        modeRow.add(solo);
        modeRow.add(duel);
        add(modeRow);

        add(Box.createVerticalStrut(22));

        RosterLine roster = new RosterLine();
        roster.setAlignmentX(CENTER_ALIGNMENT);
        add(roster);

        add(Box.createVerticalStrut(10));

        JLabel note = new JLabel("原始控制台规则 · 技能数值完全保留 · 复活每局仅一次");
        note.setFont(Theme.tracked(Theme.UI_TINY, 0.04f));
        note.setForeground(Theme.FAINT);
        note.setAlignmentX(CENTER_ALIGNMENT);
        add(note);

        add(Box.createVerticalStrut(6));

        TextLinkButton exit = new TextLinkButton("退出游戏");
        exit.setAlignmentX(CENTER_ALIGNMENT);
        exit.addActionListener(event -> listener.onExit());
        add(exit);

        add(Box.createVerticalGlue());
    }

    /** 主页底部的小型角色一览。 */
    private static final class RosterLine extends JComponent {

        private final List<Segment> playable = List.of(
                new Segment("不死者", Theme.VIOLET),
                new Segment("家成", Theme.AMBER)
        );
        private final Segment enemy = new Segment("deepseek", Theme.RED);

        RosterLine() {
            setOpaque(false);
            setPreferredSize(new Dimension(520, 32));
            setMaximumSize(new Dimension(800, 32));
            setMinimumSize(new Dimension(360, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.quality(g2);
            g2.setFont(Theme.UI_SMALL);
            FontMetrics metrics = g2.getFontMetrics();

            String prefix = "可选角色";
            String divider = "／";
            String suffix = "敌人";
            int gap = 16;
            int segmentWidth = 6 + 8;
            int prefixWidth = metrics.stringWidth(prefix);
            int dividerWidth = metrics.stringWidth(divider);
            int suffixWidth = metrics.stringWidth(suffix);

            int total = prefixWidth + gap + dividerWidth + gap + suffixWidth + gap;
            for (Segment segment : playable) {
                total += segmentWidth + metrics.stringWidth(segment.label) + gap;
            }
            total += segmentWidth + metrics.stringWidth(enemy.label);

            int x = (getWidth() - total) / 2;
            int baseline = getHeight() / 2 + metrics.getAscent() / 2 - 2;

            g2.setColor(Theme.FAINT);
            g2.drawString(prefix, x, baseline);
            x += prefixWidth + gap;

            for (Segment segment : playable) {
                x = drawSegment(g2, metrics, segment, x, baseline, gap);
            }

            g2.setColor(Theme.alpha(Theme.FAINT, 170));
            g2.drawString(divider, x, baseline);
            x += dividerWidth + gap;

            g2.setColor(Theme.FAINT);
            g2.drawString(suffix, x, baseline);
            x += suffixWidth + gap;

            drawSegment(g2, metrics, enemy, x, baseline, 0);
            g2.dispose();
        }

        private int drawSegment(Graphics2D g2, FontMetrics metrics, Segment segment,
                                int x, int baseline, int trailingGap) {
            g2.setColor(Theme.alpha(segment.color, 210));
            g2.fillOval(x, baseline - 7, 6, 6);
            x += 6 + 8;
            g2.setFont(Theme.UI_SMALL);
            g2.setColor(segment.color);
            g2.drawString(segment.label, x, baseline);
            return x + metrics.stringWidth(segment.label) + trailingGap;
        }

        private record Segment(String label, Color color) {
        }
    }
}
