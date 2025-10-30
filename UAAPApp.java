import javax.swing.*;

public class UAAPApp extends JFrame {

    public UAAPApp() {
        setTitle("UAAP Management System");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tabbed UI so you can add more modules later
        JTabbedPane tabs = new JTabbedPane();

        // Events tab
        tabs.addTab("Events", new EventManagerPanel());

        tabs.addTab("Matches", new MatchManagerPanel());
        tabs.addTab("Match Teams", new MatchTeamManagerPanel());
        tabs.addTab("Quarter Scores (BB)", new MatchQuarterScoreManagerPanel());
        tabs.addTab("Set Scores (VB)", new MatchSetScoreManagerPanel());
        tabs.addTab("Seat & Ticket", new SeatAndTicketManagerPanel());
        tabs.addTab("Event Personnel", new EventPersonnelManagerPanel());
        tabs.addTab("Teams", new TeamManagerPanel());
        tabs.addTab("Players", new PlayerManagerPanel());

        add(tabs);
    }

    public static void main(String[] args) {
        // Required for nicer look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {}

        SwingUtilities.invokeLater(() -> {
            new UAAPApp().setVisible(true);
        });
    }
}
