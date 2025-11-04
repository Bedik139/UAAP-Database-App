import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class CustomerPortalFrame extends JFrame {

    public CustomerPortalFrame() {
        setTitle("UAAP Customer Portal");
        setSize(960, 640);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Purchase Ticket", new TicketPurchasePanel());
        tabs.addTab("Request Refund", new TicketRefundPanel());

        add(tabs);
    }

    public static void showUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {}

        SwingUtilities.invokeLater(() -> new CustomerPortalFrame().setVisible(true));
    }
}
