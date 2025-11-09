import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Window;

public final class UAAPApp {

    private UAAPApp() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
        }
        UAAPTheme.applyModernDefaults();
        showRoleSelection();
    }

    public static void showRoleSelection() {
        Runnable flow = () -> new UAAPMainMenuFrame().setVisible(true);

        if (SwingUtilities.isEventDispatchThread()) {
            flow.run();
        } else {
            SwingUtilities.invokeLater(flow);
        }
    }

    public static void navigateToMainMenu(Window currentWindow) {
        SwingUtilities.invokeLater(() -> {
            if (currentWindow != null) {
                currentWindow.dispose();
            }
            showRoleSelection();
        });
    }
}
