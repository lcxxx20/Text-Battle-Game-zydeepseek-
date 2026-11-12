package javawzgdyx.ui;

import javawzgdyx.game.BattleView;
import javawzgdyx.game.GameMode;
import javawzgdyx.game.LogTone;
import javawzgdyx.model.Role;
import javawzgdyx.model.Skill;
import javawzgdyx.ui.components.FighterCard;
import javawzgdyx.ui.components.LogPane;
import javawzgdyx.ui.components.PillLabel;
import javawzgdyx.ui.components.RoundedButton;
import javawzgdyx.ui.components.RoundedPanel;
import javawzgdyx.ui.components.SkillCardButton;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;

/** 战斗主界面，同时实现 BattleView 供控制器驱动。 */
public class BattlePanel extends JPanel implements BattleView {

    public interface Listener {
        void onQuit();

        void onResult(boolean victory, String message);
    }

    private final Listener listener;
    private final JLabel phaseLabel = new JLabel("战斗准备");
    private final PillLabel modeChip = new PillLabel("MODE 01");
    private final FighterCard leftCard = new FighterCard();
    private final FighterCard rightCard = new FighterCard();
    private final LogPane logPane = new LogPane();
    private final JLabel actionTitle = new JLabel("战斗准备");
    private final JLabel actionSub = new JLabel("正在进入战场……");
    private final JPanel actionCards = new JPanel();
    private RoundedPanel actionPanel;

    private Role left;
    private Role right;
    private GameMode mode;
    private int requestId;
    private boolean choosing;

    public BattlePanel(Listener listener) {
        this.listener = listener;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(4, 28, 18, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "quit-battle");
        getActionMap().put("quit-battle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                listener.onQuit();
            }
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        RoundedButton quit = new RoundedButton("←  退出本局", RoundedButton.Style.GHOST, 10).compact();
        quit.addActionListener(event -> listener.onQuit());
        JPanel west = new JPanel(new GridBagLayout());
        west.setOpaque(false);
        west.add(quit);
        header.add(west, BorderLayout.WEST);

        phaseLabel.setFont(Theme.tracked(Theme.UI_TINY.deriveFont(11f), 0.14f));
        phaseLabel.setForeground(Theme.alpha(Theme.GOLD, 210));
        phaseLabel.setHorizontalAlignment(JLabel.CENTER);
        header.add(phaseLabel, BorderLayout.CENTER);

        JPanel east = new JPanel(new GridBagLayout());
        east.setOpaque(false);
        east.add(modeChip);
        header.add(east, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        JPanel arena = new JPanel(new GridBagLayout());
        arena.setOpaque(false);
        arena.setPreferredSize(new Dimension(10, 232));
        arena.setMinimumSize(new Dimension(10, 200));

        GridBagConstraints leftConstraints = new GridBagConstraints();
        leftConstraints.gridx = 0;
        leftConstraints.gridy = 0;
        leftConstraints.weightx = 1;
        leftConstraints.weighty = 1;
        leftConstraints.fill = GridBagConstraints.BOTH;
        arena.add(leftCard, leftConstraints);

        GridBagConstraints vsConstraints = new GridBagConstraints();
        vsConstraints.gridx = 1;
        vsConstraints.gridy = 0;
        vsConstraints.weightx = 0;
        vsConstraints.weighty = 1;
        vsConstraints.fill = GridBagConstraints.VERTICAL;
        vsConstraints.insets = new Insets(0, 10, 0, 10);
        arena.add(new VersusBadge(), vsConstraints);

        GridBagConstraints rightConstraints = new GridBagConstraints();
        rightConstraints.gridx = 2;
        rightConstraints.gridy = 0;
        rightConstraints.weightx = 1;
        rightConstraints.weighty = 1;
        rightConstraints.fill = GridBagConstraints.BOTH;
        arena.add(rightCard, rightConstraints);

        body.add(arena, BorderLayout.NORTH);
        body.add(logPane, BorderLayout.CENTER);
        actionPanel = buildActionPanel();
        body.add(actionPanel, BorderLayout.SOUTH);
        return body;
    }

    private RoundedPanel buildActionPanel() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
        panel.setPreferredSize(new Dimension(10, 172));
        panel.setMinimumSize(new Dimension(10, 150));

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        actionTitle.setFont(Theme.tracked(new java.awt.Font("KaiTi", java.awt.Font.BOLD, 17), 0.03f));
        actionTitle.setForeground(Theme.TEXT);
        header.add(actionTitle, BorderLayout.WEST);

        actionSub.setFont(Theme.UI_TINY);
        actionSub.setForeground(Theme.MUTED);
        actionSub.setHorizontalAlignment(JLabel.RIGHT);
        header.add(actionSub, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        actionCards.setOpaque(false);
        actionCards.setLayout(new GridLayout(1, 1, 10, 0));
        actionCards.setBorder(BorderFactory.createEmptyBorder());
        panel.add(actionCards, BorderLayout.CENTER);
        return panel;
    }

    /* ------------------------------------------------------------------ */
    /* BattleView                                                          */
    /* ------------------------------------------------------------------ */

    @Override
    public void beginBattle(Role left, Role right, GameMode mode) {
        this.left = left;
        this.right = right;
        this.mode = mode;
        this.requestId++;
        this.choosing = false;

        leftCard.setRole(left, "PLAYER 1", false);
        rightCard.setRole(right, mode.isDuel() ? "PLAYER 2" : "ENEMY", true);
        modeChip.setText(mode.getTag());
        if (mode.isDuel()) {
            modeChip.setColors(Theme.RED, Theme.alpha(Theme.RED, 16), Theme.alpha(Theme.RED, 90));
        } else {
            modeChip.setColors(Theme.GOLD, Theme.alpha(Theme.GOLD, 14), Theme.alpha(Theme.GOLD, 80));
        }

        logPane.clear();
        actionTitle.setText("战斗准备");
        actionSub.setText("正在进入战场……");
        clearActionCards();
        setPhase("战斗准备");
        setActiveRole(null);
    }

    @Override
    public void setPhase(String text) {
        phaseLabel.setText(text);
    }

    @Override
    public void setActiveRole(Role role) {
        leftCard.setActive(role != null && role == left);
        rightCard.setActive(role != null && role == right);
    }

    @Override
    public void refreshRoles() {
        leftCard.refresh();
        rightCard.refresh();
    }

    @Override
    public void log(String text, LogTone tone) {
        logPane.append(text, tone);
    }

    @Override
    public void requestSkillChoice(Role actor, List<Skill> skills, SkillHandler handler) {
        int token = ++requestId;
        choosing = true;
        actionTitle.setText(actor.getName() + " · 选择技能");
        actionSub.setText("点击技能卡发动行动");
        setPhase(actor.getName() + " · 选择技能");

        actionCards.removeAll();
        actionCards.setLayout(new GridLayout(1, Math.max(1, skills.size()), 10, 0));
        for (Skill skill : skills) {
            SkillCardButton button = new SkillCardButton(skill.name(), skill.description(), skill.tag());
            button.addActionListener(event -> choose(token, () -> handler.onSkillChosen(skill)));
            actionCards.add(button);
        }
        actionCards.revalidate();
        actionCards.repaint();
    }

    @Override
    public void requestAttackerChoice(Role left, Role right, AttackerHandler handler) {
        int token = ++requestId;
        choosing = true;
        setActiveRole(null);
        actionTitle.setText("双人对战 · 选择行动");
        actionSub.setText("点击角色卡决定由谁出手");
        setPhase("双人对战 · 选择行动");

        actionCards.removeAll();
        actionCards.setLayout(new GridLayout(1, 2, 10, 0));

        SkillCardButton first = new SkillCardButton(
                left.getName() + " 先手",
                "由 " + left.getName() + " 选择技能，攻击 " + right.getName() + "。",
                "出手");
        first.addActionListener(event -> choose(token, () -> handler.onAttackerChosen(left)));
        actionCards.add(first);

        SkillCardButton second = new SkillCardButton(
                right.getName() + " 先手",
                "由 " + right.getName() + " 选择技能，攻击 " + left.getName() + "。",
                "出手");
        second.addActionListener(event -> choose(token, () -> handler.onAttackerChosen(right)));
        actionCards.add(second);

        actionCards.revalidate();
        actionCards.repaint();
    }

    @Override
    public void animateDamage(Role target, int damage) {
        if (target == left) {
            leftCard.flashDamage(damage);
        } else if (target == right) {
            rightCard.flashDamage(damage);
        }
    }

    @Override
    public void animateHeal(Role target, int amount) {
        if (target == left) {
            leftCard.flashHeal(amount);
        } else if (target == right) {
            rightCard.flashHeal(amount);
        }
    }

    @Override
    public void endBattle() {
        choosing = false;
        requestId++;
        clearActionCards();
        setActiveRole(null);
        setPhase("战斗结束");
        actionTitle.setText("战斗结束");
        actionSub.setText("正在结算……");
    }

    @Override
    public void showResult(boolean victory, String message) {
        listener.onResult(victory, message);
    }

    private void choose(int token, Runnable action) {
        if (!choosing || token != requestId) {
            return;
        }
        choosing = false;
        requestId++;
        actionCards.removeAll();
        actionCards.revalidate();
        actionCards.repaint();
        actionTitle.setText("行动结算中……");
        actionSub.setText("……");
        action.run();
    }

    private void clearActionCards() {
        choosing = false;
        requestId++;
        actionCards.removeAll();
        actionCards.revalidate();
        actionCards.repaint();
    }

    /** 左右角色卡之间的 VS 竖线与徽记。 */
    private static final class VersusBadge extends JComponent {

        VersusBadge() {
            setPreferredSize(new Dimension(74, 230));
            setMinimumSize(new Dimension(64, 200));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.quality(g2);
            int width = getWidth();
            int height = getHeight();
            int cx = width / 2;

            g2.setPaint(new java.awt.GradientPaint(cx, 12, new Color(255, 255, 255, 0),
                    cx, height / 2 - 34, new Color(255, 255, 255, 34)));
            g2.drawLine(cx, 12, cx, height / 2 - 34);
            g2.setPaint(new java.awt.GradientPaint(cx, height / 2 + 34, new Color(255, 255, 255, 34),
                    cx, height - 12, new Color(255, 255, 255, 0)));
            g2.drawLine(cx, height / 2 + 34, cx, height - 12);

            int size = 58;
            int x = cx - size / 2;
            int y = height / 2 - size / 2;
            g2.setColor(new Color(8, 11, 20, 230));
            g2.fillOval(x, y, size, size);
            g2.setColor(Theme.alpha(Theme.GOLD, 50));
            g2.setStroke(new BasicStroke(3f));
            g2.drawOval(x - 2, y - 2, size + 4, size + 4);
            g2.setColor(Theme.alpha(Theme.GOLD, 170));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(x, y, size, size);

            g2.setFont(Theme.tracked(Theme.MONO_BOLD, 0.12f));
            g2.setColor(Theme.GOLD_SOFT);
            java.awt.FontMetrics metrics = g2.getFontMetrics();
            String text = "VS";
            g2.drawString(text, cx - metrics.stringWidth(text) / 2,
                    height / 2 + metrics.getAscent() / 2 - 2);

            g2.setColor(Theme.alpha(Theme.GOLD, 70));
            int diamond = 3;
            g2.fillPolygon(new int[]{cx, cx + diamond, cx, cx - diamond},
                    new int[]{height / 2 - 44, height / 2 - 41, height / 2 - 38, height / 2 - 41}, 4);
            g2.fillPolygon(new int[]{cx, cx + diamond, cx, cx - diamond},
                    new int[]{height / 2 + 38, height / 2 + 41, height / 2 + 44, height / 2 + 41}, 4);

            g2.dispose();
        }
    }
}
