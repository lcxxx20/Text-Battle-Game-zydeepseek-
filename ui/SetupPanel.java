package javawzgdyx.ui;

import javawzgdyx.game.GameMode;
import javawzgdyx.game.PlayerConfig;
import javawzgdyx.model.CharacterClass;
import javawzgdyx.ui.components.ClassCard;
import javawzgdyx.ui.components.DarkTextField;
import javawzgdyx.ui.components.PreviewPanel;
import javawzgdyx.ui.components.RoundedButton;
import javawzgdyx.ui.components.RoundedPanel;
import javawzgdyx.ui.components.SegmentedToggle;
import javawzgdyx.ui.components.StepPips;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/** 选角界面：左侧职业卡，右侧姓名 / 性别 / 预览。 */
public class SetupPanel extends JPanel {

    public interface Listener {
        void onBack();

        void onConfirm(PlayerConfig config);
    }

    private final Listener listener;
    private final JLabel eyebrowLabel = new JLabel();
    private final JLabel titleLabel = new JLabel();
    private final StepPips pips = new StepPips();
    private final List<ClassCard> classCards = new ArrayList<>();
    private final DarkTextField nameField = new DarkTextField("给你的角色起个名字");
    private final SegmentedToggle maleToggle = new SegmentedToggle("男");
    private final SegmentedToggle femaleToggle = new SegmentedToggle("女");
    private final PreviewPanel preview = new PreviewPanel();
    private final JLabel errorLabel = new JLabel(" ");
    private final RoundedButton confirmButton = new RoundedButton("确认出战", RoundedButton.Style.PRIMARY);

    private CharacterClass selectedClass = CharacterClass.BUSIZHE;
    private char sex = '男';
    private GameMode mode = GameMode.SOLO;
    private int step;
    private int totalSteps = 1;

    public SetupPanel(Listener listener) {
        this.listener = listener;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 28, 20, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                changed();
            }

            private void changed() {
                clearError();
                updatePreview();
            }
        });

        maleToggle.addActionListener(event -> {
            sex = '男';
            updatePreview();
        });
        femaleToggle.addActionListener(event -> {
            sex = '女';
            updatePreview();
        });
        confirmButton.addActionListener(event -> submit());
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(16, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 14, 0));

        RoundedButton back = new RoundedButton("←  返回", RoundedButton.Style.GHOST, 10).compact();
        back.addActionListener(event -> listener.onBack());
        JPanel backWrap = new JPanel(new GridBagLayout());
        backWrap.setOpaque(false);
        backWrap.add(back);
        wrapper.add(backWrap, BorderLayout.WEST);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        eyebrowLabel.setFont(Theme.tracked(Theme.UI_TINY.deriveFont(10f), 0.18f));
        eyebrowLabel.setForeground(Theme.alpha(Theme.GOLD, 200));
        eyebrowLabel.setAlignmentX(LEFT_ALIGNMENT);
        titleLabel.setFont(new java.awt.Font("KaiTi", java.awt.Font.BOLD, 29));
        titleLabel.setForeground(Theme.TEXT);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        heading.add(eyebrowLabel);
        heading.add(Box.createVerticalStrut(5));
        heading.add(titleLabel);
        wrapper.add(heading, BorderLayout.CENTER);

        JPanel pipsWrap = new JPanel(new GridBagLayout());
        pipsWrap.setOpaque(false);
        pipsWrap.add(pips);
        wrapper.add(pipsWrap, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        GridBagConstraints leftConstraints = new GridBagConstraints();
        leftConstraints.gridx = 0;
        leftConstraints.gridy = 0;
        leftConstraints.weightx = 0.56;
        leftConstraints.weighty = 1;
        leftConstraints.fill = GridBagConstraints.BOTH;
        leftConstraints.insets = new Insets(0, 0, 0, 18);
        body.add(buildClassColumn(), leftConstraints);

        GridBagConstraints rightConstraints = new GridBagConstraints();
        rightConstraints.gridx = 1;
        rightConstraints.gridy = 0;
        rightConstraints.weightx = 0.44;
        rightConstraints.weighty = 1;
        rightConstraints.fill = GridBagConstraints.BOTH;
        body.add(buildForm(), rightConstraints);

        return body;
    }

    private JPanel buildClassColumn() {
        JPanel column = new JPanel(new GridBagLayout());
        column.setOpaque(false);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 8, 0);
        column.add(fieldLabel("选择职业"), constraints);

        JPanel cards = new JPanel(new GridLayout(2, 1, 0, 14));
        cards.setOpaque(false);
        cards.setMinimumSize(new Dimension(320, 280));
        for (CharacterClass characterClass : CharacterClass.playable()) {
            ClassCard card = new ClassCard(characterClass);
            card.addActionListener(event -> selectClass(characterClass));
            classCards.add(card);
            cards.add(card);
        }
        constraints.gridy = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 12, 0);
        column.add(cards, constraints);

        constraints.gridy = 2;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        column.add(buildRuleCard(), constraints);
        return column;
    }

    private RoundedPanel buildRuleCard() {
        RoundedPanel rule = new RoundedPanel(new BorderLayout(0, 4));
        rule.setBorder(new EmptyBorder(10, 14, 10, 14));
        rule.setPreferredSize(new Dimension(300, 78));
        rule.setMinimumSize(new Dimension(200, 70));

        JLabel heading = new JLabel("战斗速览");
        heading.setFont(Theme.tracked(Theme.UI_TINY.deriveFont(java.awt.Font.BOLD, 10f), 0.1f));
        heading.setForeground(Theme.alpha(Theme.GOLD, 190));
        rule.add(heading, BorderLayout.NORTH);

        JLabel body = new JLabel("<html><div style='width:320px;line-height:1.4'>生命值随机 100~199，初始 20 回合。<br>模式 02 没有回合上限，先倒下的一方告负。</div></html>");
        body.setFont(Theme.UI_TINY);
        body.setForeground(Theme.MUTED);
        rule.add(body, BorderLayout.CENTER);
        return rule;
    }

    private JPanel buildForm() {
        RoundedPanel form = new RoundedPanel(new BorderLayout());
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 22, 20, 22));

        form.add(fieldLabel("角色姓名"));
        form.add(Box.createVerticalStrut(8));
        nameField.setAlignmentX(LEFT_ALIGNMENT);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        nameField.setMinimumSize(new Dimension(160, 46));
        form.add(nameField);

        form.add(Box.createVerticalStrut(17));
        form.add(fieldLabel("性别"));
        form.add(Box.createVerticalStrut(8));

        JPanel sexRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sexRow.setOpaque(false);
        sexRow.setAlignmentX(LEFT_ALIGNMENT);
        sexRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        ButtonGroup group = new ButtonGroup();
        group.add(maleToggle);
        group.add(femaleToggle);
        maleToggle.setSelected(true);
        sexRow.add(maleToggle);
        sexRow.add(femaleToggle);
        form.add(sexRow);

        form.add(Box.createVerticalStrut(18));
        preview.setAlignmentX(LEFT_ALIGNMENT);
        form.add(preview);

        JLabel tip = new JLabel("<html><div style='width:300px'>姓名不能包含空格；复活技能每局只能使用一次。</div></html>");
        tip.setFont(Theme.UI_TINY);
        tip.setForeground(Theme.FAINT);
        tip.setAlignmentX(LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(10));
        form.add(tip);

        form.add(Box.createVerticalGlue());

        errorLabel.setFont(Theme.UI_TINY.deriveFont(11f));
        errorLabel.setForeground(Theme.RED);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(6));

        confirmButton.setAlignmentX(LEFT_ALIGNMENT);
        confirmButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        confirmButton.setMinimumSize(new Dimension(180, 46));
        confirmButton.setPreferredSize(new Dimension(280, 46));
        form.add(confirmButton);

        return form;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.tracked(Theme.UI_BOLD_SMALL, 0.08f));
        label.setForeground(Theme.alpha(Theme.GOLD, 205));
        return label;
    }

    public void configure(GameMode mode, int step, String playerLabel, PlayerConfig initial) {
        this.mode = mode;
        this.step = step;
        this.totalSteps = mode.getPlayerCount();

        eyebrowLabel.setText(mode.getTag() + " · " + (mode.isDuel() ? "同屏对战" : mode.getTitle()));
        if (mode.isDuel()) {
            titleLabel.setText("创建角色 " + (step == 0 ? "A" : "B"));
        } else {
            titleLabel.setText("选择你的角色");
        }

        String[] labels;
        if (mode.isDuel()) {
            labels = new String[]{"1  角色A", "2  角色B"};
        } else {
            labels = new String[]{"1  " + playerLabel};
        }
        pips.setSteps(labels, step);

        selectedClass = initial.characterClass();
        sex = initial.sex();
        nameField.setText(initial.name());
        nameField.setPlaceholder(mode.isDuel()
                ? "给「" + playerLabel + "」起个名字"
                : "给你的角色起个名字");

        if (step < totalSteps - 1) {
            confirmButton.setText("下一步  →");
        } else if (mode.isDuel()) {
            confirmButton.setText("开始对战  →");
        } else {
            confirmButton.setText("确认出战  →");
        }

        updateSelection();
        updateSexToggles();
        updatePreview();
        clearError();
        revalidate();
        repaint();
        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    private void selectClass(CharacterClass characterClass) {
        selectedClass = characterClass;
        updateSelection();
        updatePreview();
    }

    private void updateSelection() {
        for (ClassCard card : classCards) {
            card.setSelected(card.getCharacterClass() == selectedClass);
        }
    }

    private void updateSexToggles() {
        maleToggle.setSelected(sex == '男');
        femaleToggle.setSelected(sex == '女');
    }

    private void updatePreview() {
        preview.setDraft(selectedClass, nameField.getText().trim(), sex);
    }

    private void submit() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("先给角色起个名字吧。");
            nameField.requestFocusInWindow();
            return;
        }
        if (name.codePoints().anyMatch(Character::isWhitespace)) {
            showError("姓名里不能包含空格（与原版控制台输入一致）。");
            nameField.requestFocusInWindow();
            return;
        }
        clearError();
        listener.onConfirm(new PlayerConfig(selectedClass, name, sex));
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.repaint();
    }

    private void clearError() {
        errorLabel.setText(" ");
    }
}
