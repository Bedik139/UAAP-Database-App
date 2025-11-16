import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ManagerDashboardFrame extends JFrame {

    public ManagerDashboardFrame() {
        setTitle("UAAP Manager Dashboard");
        setSize(1180, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UAAPTheme.LIGHT_SURFACE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Events", new EventManagerPanel());
        tabs.addTab("Matches", new MatchManagerPanel());
        tabs.addTab("Match Teams", new MatchTeamManagerPanel());
        tabs.addTab("Quarter Scores (BB)", new MatchQuarterScoreManagerPanel());
        tabs.addTab("Set Scores (VB)", new MatchSetScoreManagerPanel());
        tabs.addTab("Seat & Ticket", new SeatAndTicketManagerPanel());
        tabs.addTab("Refund Audit", new RefundAuditPanel());
        tabs.addTab("Event Personnel", new EventPersonnelManagerPanel());
        tabs.addTab("Teams", new TeamManagerPanel());
        tabs.addTab("Players", new PlayerManagerPanel());
        tabs.addTab("Reports", new ReportsPanel());

        UAAPTheme.styleTabPane(tabs);
        UAAPTheme.elevate(tabs);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        contentPanel.add(tabs, BorderLayout.CENTER);

        root.add(contentPanel, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        
        JButton mainMenuButton = new JButton("Main Menu");
        UAAPTheme.styleNeutralButton(mainMenuButton);
        mainMenuButton.addActionListener(e -> UAAPApp.navigateToMainMenu(this));
        
        buttonPanel.add(mainMenuButton);
        root.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    public static void showUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
        }
        UAAPTheme.applyModernDefaults();

        SwingUtilities.invokeLater(() -> new ManagerDashboardFrame().setVisible(true));
    }
}
