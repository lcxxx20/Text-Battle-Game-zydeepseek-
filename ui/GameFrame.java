package javawzgdyx.ui;

import javawzgdyx.game.BattleController;
import javawzgdyx.game.GameMode;
import javawzgdyx.game.PlayerConfig;
import javawzgdyx.model.CharacterClass;
import javawzgdyx.model.Role;
import javawzgdyx.model.Zayu;
import javawzgdyx.ui.components.BackgroundPanel;
import javawzgdyx.ui.components.BrandBar;
import javawzgdyx.ui.components.ResultOverlay;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/** 应用主窗口，负责页面切换与选角流程。 */
public class GameFrame extends JFrame {

    private static final String CARD_MENU = "menu";
    private static final String CARD_SETUP = "setup";
    private static final String CARD_BATTLE = "battle";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel screens = new JPanel(cardLayout);
    private final MainMenuPanel menuPanel;
    private final SetupPanel setupPanel;
    private final ResultOverlay resultOverlay;

    private BattlePanel battlePanel;
    private BattleController controller;

    private GameMode currentMode;
    private int setupStep;
    private final List<PlayerConfig> drafts = new ArrayList<>();
    private PlayerConfig draft = new PlayerConfig(CharacterClass.BUSIZHE, "", '男');

    private GameMode lastMode;
    private List<PlayerConfig> lastConfigs;

    public GameFrame() {
        super("文字格斗 · Java Swing Edition");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 760));
        setSize(1240, 860);
        setLocationRelativeTo(null);
        setIconImage(Theme.appIcon());

        BackgroundPanel root = new BackgroundPanel(new BorderLayout());
        root.add(new BrandBar(), BorderLayout.NORTH);

        screens.setOpaque(false);
        root.add(screens, BorderLayout.CENTER);
        setContentPane(root);

        menuPanel = new MainMenuPanel(new MainMenuPanel.Listener() {
            @Override
            public void onMode(GameMode mode) {
                openSetup(mode);
            }

            @Override
            public void onExit() {
                confirmExit();
            }
        });

        setupPanel = new SetupPanel(new SetupPanel.Listener() {
            @Override
            public void onBack() {
                handleSetupBack();
            }

            @Override
            public void onConfirm(PlayerConfig config) {
                handleSetupConfirm(config);
            }
        });

        screens.add(menuPanel, CARD_MENU);
        screens.add(setupPanel, CARD_SETUP);

        resultOverlay = new ResultOverlay();
        setGlassPane(resultOverlay);
        resultOverlay.setVisible(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                stopBattle();
            }
        });

        showMenu();
    }

    /* ------------------------------------------------------------------ */
    /* 页面                                                                */
    /* ------------------------------------------------------------------ */

    private void openSetup(GameMode mode) {
        stopBattle();
        resultOverlay.setVisible(false);
        currentMode = mode;
        setupStep = 0;
        drafts.clear();
        draft = new PlayerConfig(CharacterClass.BUSIZHE, "", '男');
        configureSetup();
        cardLayout.show(screens, CARD_SETUP);
    }

    private void configureSetup() {
        String playerLabel = currentMode.isDuel()
                ? (setupStep == 0 ? "角色A" : "角色B")
                : "你";
        setupPanel.configure(currentMode, setupStep, playerLabel, draft);
    }

    private void handleSetupBack() {
        if (currentMode == GameMode.DUEL && setupStep == 1) {
            setupStep = 0;
            if (!drafts.isEmpty()) {
                draft = drafts.remove(drafts.size() - 1);
            }
            configureSetup();
            return;
        }
        showMenu();
    }

    private void handleSetupConfirm(PlayerConfig config) {
        drafts.add(config);
        if (currentMode == GameMode.DUEL && setupStep == 0) {
            setupStep = 1;
            draft = new PlayerConfig(CharacterClass.BUSIZHE, "", '男');
            configureSetup();
            return;
        }
        startBattle(currentMode, List.copyOf(drafts));
    }

    private void showMenu() {
        stopBattle();
        resultOverlay.setVisible(false);
        if (battlePanel != null) {
            screens.remove(battlePanel);
            battlePanel = null;
        }
        currentMode = null;
        setupStep = 0;
        drafts.clear();
        cardLayout.show(screens, CARD_MENU);
    }

    /* ------------------------------------------------------------------ */
    /* 战斗                                                                */
    /* ------------------------------------------------------------------ */

    private void startBattle(GameMode mode, List<PlayerConfig> configs) {
        stopBattle();
        resultOverlay.setVisible(false);

        lastMode = mode;
        lastConfigs = List.copyOf(configs);

        Role left = configs.get(0).createRole();
        Role right = mode.isDuel() ? configs.get(1).createRole() : new Zayu("deepseek");

        if (battlePanel != null) {
            screens.remove(battlePanel);
        }
        battlePanel = new BattlePanel(new BattlePanel.Listener() {
            @Override
            public void onQuit() {
                quitBattle();
            }

            @Override
            public void onResult(boolean victory, String message) {
                showBattleResult(victory, message);
            }
        });
        screens.add(battlePanel, CARD_BATTLE);
        cardLayout.show(screens, CARD_BATTLE);

        controller = new BattleController(battlePanel, mode, left, right);
        controller.start();
    }

    private void quitBattle() {
        if (resultOverlay.isVisible()) {
            return;
        }
        if (battlePanel == null || !battlePanel.isShowing()) {
            return;
        }
        showMenu();
    }

    private void showBattleResult(boolean victory, String message) {
        resultOverlay.showResult(victory, message, this::rematchBattle, this::returnToMenu);
        resultOverlay.setVisible(true);
        resultOverlay.requestFocusInWindow();
    }

    private void rematchBattle() {
        if (lastMode == null || lastConfigs == null) {
            showMenu();
            return;
        }
        startBattle(lastMode, lastConfigs);
    }

    private void returnToMenu() {
        showMenu();
    }

    private void stopBattle() {
        if (controller != null) {
            controller.cancel();
            controller = null;
        }
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "确定要退出《文字格斗》吗？",
                "文字格斗",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            stopBattle();
            dispose();
            System.exit(0);
        }
    }
}
