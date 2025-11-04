import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class UAAPApp {

    private UAAPApp() {}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {}

        SwingUtilities.invokeLater(() -> {
            Object[] roles = {"Manager", "Customer"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Welcome to the UAAP system. Please choose your portal:",
                    "UAAP Portal Selection",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    roles,
                    roles[0]
            );

            if (choice == 1) {
                CustomerPortalFrame.showUI();
            } else if (choice == 0) {
                ManagerDashboardFrame.showUI();
            }
        });
    }
}
