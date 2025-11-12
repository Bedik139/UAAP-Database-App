import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class CustomerPortalFrame extends JFrame {

    public CustomerPortalFrame() {
        setTitle("UAAP Customer Portal");
        setSize(1024, 660);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UAAPTheme.LIGHT_SURFACE);

        UAAPHeaderPanel headerPanel = new UAAPHeaderPanel(
                "UAAP Customer Portal",
                "Secure verified tickets for UAAP events",
                () -> UAAPApp.navigateToMainMenu(this)
        );
        root.add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Purchase Ticket", new TicketPurchasePanel());

        UAAPTheme.styleTabPane(tabs);
        UAAPTheme.elevate(tabs);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));
        contentPanel.add(tabs, BorderLayout.CENTER);

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    public static void showUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
        }
        UAAPTheme.applyModernDefaults();

        SwingUtilities.invokeLater(() -> new CustomerPortalFrame().setVisible(true));
    }
}
