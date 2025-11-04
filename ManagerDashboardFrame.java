import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ManagerDashboardFrame extends JFrame {

    public ManagerDashboardFrame() {
        setTitle("UAAP Manager Dashboard");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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

        add(tabs);
    }

    public static void showUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {}

        SwingUtilities.invokeLater(() -> new ManagerDashboardFrame().setVisible(true));
    }
}
