import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final ReportService reportService = new ReportService();

    public ReportsPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Season Standings", buildSeasonStandingsTab());
        tabs.addTab("Ticket Sales", buildTicketSalesTab());
        tabs.addTab("Venue Utilization", buildVenueUtilizationTab());
        tabs.addTab("Ticket Revenue", buildRevenueTab());
        tabs.addTab("Player Statistics", buildPlayerStatsTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildSeasonStandingsTab() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Team", "Wins", "Losses", "Games Played", "Win %"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            try {
                List<ReportService.TeamStandingRow> rows = reportService.getSeasonStandings();
                for (ReportService.TeamStandingRow row : rows) {
                    model.addRow(new Object[]{
                            row.teamName(),
                            row.wins(),
                            row.losses(),
                            row.gamesPlayed(),
                            row.winPercentage()
                    });
                }
            } catch (Exception ex) {
                showError("Unable to load standings:\n" + ex.getMessage());
            }
        });
        refresh.doClick();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        control.add(refresh);
        panel.add(control, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTicketSalesTab() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Sale Day", "Event", "Sport", "Match ID", "Tickets Sold", "Revenue"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);

        JTextField fromField = new JTextField(10);
        JTextField toField = new JTextField(10);

        JButton refresh = new JButton("Generate");
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            try {
                LocalDate from = parseDate(fromField.getText());
                LocalDate to = parseDate(toField.getText());
                List<ReportService.TicketSalesRow> rows = reportService.getTicketSalesSummary(from, to);
                for (ReportService.TicketSalesRow row : rows) {
                    model.addRow(new Object[]{
                            row.saleDay(),
                            row.eventName(),
                            row.sport(),
                            row.matchId() != null ? row.matchId() : "All",
                            row.ticketsSold(),
                            row.revenue()
                    });
                }
            } catch (Exception ex) {
                showError("Unable to build ticket sales report:\n" + ex.getMessage());
            }
        });

        JPanel filter = new JPanel(new GridBagLayout());
        filter.setBorder(BorderFactory.createTitledBorder("Filters"));
        addFilterField(filter, 0, "From (YYYY-MM-DD)", fromField);
        addFilterField(filter, 1, "To (YYYY-MM-DD)", toField);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(6, 2, 6, 2);
        gbc.anchor = GridBagConstraints.EAST;
        filter.add(refresh, gbc);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(filter, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildVenueUtilizationTab() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Event", "Venue", "Event Date", "Seats Sold", "Capacity", "Occupancy %"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);

        JTextField monthField = new JTextField(7);
        monthField.setToolTipText("Format: YYYY-MM");

        JButton refresh = new JButton("Generate");
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            try {
                LocalDate month = parseYearMonth(monthField.getText());
                List<ReportService.VenueUtilizationRow> rows = reportService.getVenueUtilization(month);
                for (ReportService.VenueUtilizationRow row : rows) {
                    model.addRow(new Object[]{
                            row.eventName(),
                            row.venueAddress(),
                            row.eventDate(),
                            row.seatsSold(),
                            row.totalSeats(),
                            row.occupancyPercent()
                    });
                }
            } catch (Exception ex) {
                showError("Unable to build venue utilization report:\n" + ex.getMessage());
            }
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filter.setBorder(BorderFactory.createTitledBorder("Filters"));
        filter.add(new JLabel("Month (YYYY-MM):"));
        filter.add(monthField);
        filter.add(refresh);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(filter, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRevenueTab() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Event", "Match ID", "Seat Type", "Tickets Sold", "Revenue"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);

        JTextField fromField = new JTextField(10);
        JTextField toField = new JTextField(10);
        JButton refresh = new JButton("Generate");
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            try {
                LocalDate from = parseDate(fromField.getText());
                LocalDate to = parseDate(toField.getText());
                List<ReportService.TicketRevenueRow> rows = reportService.getTicketRevenueSummary(from, to);
                for (ReportService.TicketRevenueRow row : rows) {
                    model.addRow(new Object[]{
                            row.eventName(),
                            row.matchId() != null ? row.matchId() : "All",
                            row.seatType(),
                            row.ticketsSold(),
                            row.revenue()
                    });
                }
            } catch (Exception ex) {
                showError("Unable to build revenue report:\n" + ex.getMessage());
            }
        });

        JPanel filter = new JPanel(new GridBagLayout());
        filter.setBorder(BorderFactory.createTitledBorder("Filters"));
        addFilterField(filter, 0, "From (YYYY-MM-DD)", fromField);
        addFilterField(filter, 1, "To (YYYY-MM-DD)", toField);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(6, 2, 6, 2);
        gbc.anchor = GridBagConstraints.EAST;
        filter.add(refresh, gbc);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(filter, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPlayerStatsTab() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Player", "Team", "Position", "Total Points"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            try {
                List<ReportService.PlayerParticipationRow> rows = reportService.getPlayerParticipationStats();
                for (ReportService.PlayerParticipationRow row : rows) {
                    model.addRow(new Object[]{
                            row.firstName() + " " + row.lastName(),
                            row.teamName(),
                            row.position(),
                            row.totalPoints()
                    });
                }
            } catch (Exception ex) {
                showError("Unable to load player statistics:\n" + ex.getMessage());
            }
        });
        refresh.doClick();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        control.add(refresh);
        panel.add(control, BorderLayout.SOUTH);
        return panel;
    }

    private void addFilterField(JPanel panel, int row, String label, JTextField field) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(4, 6, 4, 6);
        panel.add(new JLabel(label), labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1;
        fieldGbc.insets = new Insets(4, 6, 4, 6);
        panel.add(field, fieldGbc);
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(raw.trim());
    }

    private LocalDate parseYearMonth(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        YearMonth ym = YearMonth.parse(raw.trim());
        return ym.atDay(1);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
