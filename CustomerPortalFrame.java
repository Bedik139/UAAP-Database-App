import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Purchase Ticket", new TicketPurchasePanel());
        tabs.addTab("Refund Ticket", new TicketRefundPanel());

        UAAPTheme.styleTabPane(tabs);
        UAAPTheme.elevate(tabs);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contentPanel.add(tabs, BorderLayout.CENTER);

        root.add(contentPanel, BorderLayout.CENTER);
        
        // Add Main Menu button at bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        
        JButton mainMenuButton = new JButton("← Main Menu");
        UAAPTheme.styleNeutralButton(mainMenuButton);
        mainMenuButton.addActionListener(e -> UAAPApp.navigateToMainMenu(this));
        
        bottomPanel.add(mainMenuButton);
        root.add(bottomPanel, BorderLayout.SOUTH);
        
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
