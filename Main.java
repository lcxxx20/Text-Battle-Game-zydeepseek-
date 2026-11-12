package javawzgdyx;

import javawzgdyx.ui.GameFrame;
import javawzgdyx.ui.Theme;

import javax.swing.SwingUtilities;

/** 程序入口。 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new GameFrame().setVisible(true);
        });
    }
}
