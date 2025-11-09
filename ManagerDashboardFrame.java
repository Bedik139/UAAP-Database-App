import java.awt.BorderLayout;
import javax.swing.BorderFactory;
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

        UAAPHeaderPanel headerPanel = new UAAPHeaderPanel(
                "UAAP Manager Command Center",
                "Manage the database",
                () -> UAAPApp.navigateToMainMenu(this)
        );
        root.add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Events", new EventManagerPanel());
        tabs.addTab("Matches", new MatchManagerPanel());
        tabs.addTab("Match Teams", new MatchTeamManagerPanel());
        tabs.addTab("Match Results", new MatchResultPanel());
        tabs.addTab("Quarter Scores (BB)", new MatchQuarterScoreManagerPanel());
        tabs.addTab("Set Scores (VB)", new MatchSetScoreManagerPanel());
        tabs.addTab("Seat & Ticket", new SeatAndTicketManagerPanel());
        tabs.addTab("Event Personnel", new EventPersonnelManagerPanel());
        tabs.addTab("Teams", new TeamManagerPanel());
        tabs.addTab("Players", new PlayerManagerPanel());
        tabs.addTab("Reports", new ReportsPanel());

        UAAPTheme.styleTabPane(tabs);
        UAAPTheme.elevate(tabs);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(26, 28, 28, 28));
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

        SwingUtilities.invokeLater(() -> new ManagerDashboardFrame().setVisible(true));
    }
}
