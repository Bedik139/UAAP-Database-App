import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MatchResultPanel extends JPanel {

    private final MatchDAO matchDAO = new MatchDAO();
    private final MatchTeamDAO matchTeamDAO = new MatchTeamDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final MatchResultService matchResultService = new MatchResultService();

    private JComboBox<Match> matchCombo;
    private JLabel homeTeamLabel;
    private JLabel awayTeamLabel;
    private JTextField homeScoreField;
    private JTextField awayScoreField;
    private JTextField summaryField;
    private JTextField processedByField;

    private DefaultTableModel playerModel;
    private JTable playerTable;

    private MatchTeam homeTeamInfo;
    private MatchTeam awayTeamInfo;

    public MatchResultPanel() {
        setLayout(new BorderLayout(10, 10));
        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildPlayerPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
        reloadMatches();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Match Selection"));

        matchCombo = new JComboBox<>();
        matchCombo.addActionListener(e -> loadMatchDetails());
        UAAPTheme.styleComboBox(matchCombo);

        homeTeamLabel = new JLabel("-");
        awayTeamLabel = new JLabel("-");
        homeScoreField = new JTextField(5);
        awayScoreField = new JTextField(5);
        summaryField = new JTextField(25);
        processedByField = new JTextField(20);
        
        UAAPTheme.styleTextField(homeScoreField);
        UAAPTheme.styleTextField(awayScoreField);
        UAAPTheme.styleTextField(summaryField);
        UAAPTheme.styleTextField(processedByField);

        addFormField(panel, 0, "Match", matchCombo);
        addFormField(panel, 1, "Home Team", homeTeamLabel);
        addFormField(panel, 1, "Home Score", homeScoreField, 1);
        addFormField(panel, 2, "Away Team", awayTeamLabel);
        addFormField(panel, 2, "Away Score", awayScoreField, 1);
        addFormField(panel, 3, "Score Summary (optional)", summaryField, 2);
        addFormField(panel, 4, "Processed By", processedByField, 2);

        return panel;
    }

    private JPanel buildPlayerPanel() {
        playerModel = new DefaultTableModel(new Object[]{"Player", "Team", "Points Earned"}, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        playerTable = new JTable(playerModel);
        UAAPTheme.styleTable(playerTable);
        playerTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Player Scoring Updates (optional)"));
        panel.add(new JScrollPane(playerTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtonPanel() {
        JButton commitButton = new JButton("Record Result");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh Matches");

        UAAPTheme.styleActionButton(commitButton);
        UAAPTheme.styleNeutralButton(clearButton);
        UAAPTheme.styleInfoButton(refreshButton);

        commitButton.addActionListener(e -> handleRecordResult());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> reloadMatches());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(refreshButton);
        panel.add(clearButton);
        panel.add(commitButton);
        return panel;
    }

    private void addFormField(JPanel panel, int row, String label, java.awt.Component component) {
        addFormField(panel, row, label, component, 0);
    }

    private void addFormField(JPanel panel, int row, String label, java.awt.Component component, int colOffset) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = colOffset * 2;
        labelGbc.gridy = row;
        labelGbc.insets = new Insets(6, 6, 6, 6);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = colOffset * 2 + 1;
        fieldGbc.gridy = row;
        fieldGbc.insets = new Insets(6, 6, 6, 6);
        fieldGbc.anchor = GridBagConstraints.WEST;
        if (component instanceof JTextField) {
            fieldGbc.fill = GridBagConstraints.HORIZONTAL;
            fieldGbc.weightx = 1;
        }
        panel.add(component, fieldGbc);
    }

    private void reloadMatches() {
        try {
            List<Match> matches = matchDAO.getAllMatches();
            DefaultComboBoxModel<Match> model = new DefaultComboBoxModel<>();
            for (Match match : matches) {
                model.addElement(match);
            }
            matchCombo.setModel(model);
            if (model.getSize() > 0) {
                matchCombo.setSelectedIndex(0);
            } else {
                clearMatchDetails();
            }
        } catch (SQLException ex) {
            showError("Unable to load matches:\n" + ex.getMessage());
        }
    }

    private void loadMatchDetails() {
        Match match = (Match) matchCombo.getSelectedItem();
        if (match == null) {
            clearMatchDetails();
            return;
        }

        try {
            List<MatchTeam> teams = matchTeamDAO.getMatchTeamsForMatch(match.getMatchId());
            homeTeamInfo = null;
            awayTeamInfo = null;
            for (MatchTeam team : teams) {
                if (team.isHomeTeam()) {
                    homeTeamInfo = team;
                } else {
                    awayTeamInfo = team;
                }
            }

            if (homeTeamInfo == null || awayTeamInfo == null) {
                showError("Match does not have both home and away teams configured.");
                clearMatchDetails();
                return;
            }

            homeTeamLabel.setText(homeTeamInfo.getTeamName());
            awayTeamLabel.setText(awayTeamInfo.getTeamName());
            homeScoreField.setText(String.valueOf(homeTeamInfo.getTeamScore()));
            awayScoreField.setText(String.valueOf(awayTeamInfo.getTeamScore()));
            summaryField.setText(match.getScoreSummary() != null ? match.getScoreSummary() : "");

            loadPlayers(homeTeamInfo, awayTeamInfo);
        } catch (SQLException ex) {
            showError("Unable to load match details:\n" + ex.getMessage());
        }
    }

    private void loadPlayers(MatchTeam home, MatchTeam away) throws SQLException {
        playerModel.setRowCount(0);
        List<Player> homePlayers = playerDAO.getPlayersByTeam(home.getTeamId());
        for (Player player : homePlayers) {
            playerModel.addRow(new Object[]{
                    formatPlayer(player),
                    home.getTeamName(),
                    0
            });
        }

        List<Player> awayPlayers = playerDAO.getPlayersByTeam(away.getTeamId());
        for (Player player : awayPlayers) {
            playerModel.addRow(new Object[]{
                    formatPlayer(player),
                    away.getTeamName(),
                    0
            });
        }
    }

    private String formatPlayer(Player player) {
        return String.format("%d - %s %s (ID:%d)",
                player.getPlayerNumber(),
                player.getFirstName(),
                player.getLastName(),
                player.getPlayerId());
    }

    private void handleRecordResult() {
        Match match = (Match) matchCombo.getSelectedItem();
        if (match == null) {
            showError("Select a match first.");
            return;
        }
        if (homeTeamInfo == null || awayTeamInfo == null) {
            showError("Match is missing team assignments.");
            return;
        }

        try {
            int homeScore = parseScore(homeScoreField, "Home score");
            int awayScore = parseScore(awayScoreField, "Away score");

            List<MatchResultService.PlayerStatUpdate> updates = new ArrayList<>();
            for (int row = 0; row < playerModel.getRowCount(); row++) {
                Object value = playerModel.getValueAt(row, 2);
                int points = 0;
                if (value != null && !value.toString().trim().isEmpty()) {
                    points = Integer.parseInt(value.toString().trim());
                }
                if (points > 0) {
                    int playerId = extractPlayerId(playerModel.getValueAt(row, 0).toString());
                    updates.add(new MatchResultService.PlayerStatUpdate(playerId, points));
                }
            }

            MatchResultService.MatchResultInput input = new MatchResultService.MatchResultInput(
                    match.getMatchId(),
                    homeScore,
                    awayScore,
                    summaryField.getText().trim(),
                    processedByField.getText().trim(),
                    updates
            );

            matchResultService.recordResult(input);
            JOptionPane.showMessageDialog(this, "Match result recorded.", "Success", JOptionPane.INFORMATION_MESSAGE);
            reloadMatches();
        } catch (NumberFormatException ex) {
            showError("Scores and player points must be whole numbers.\n" + ex.getMessage());
        } catch (Exception ex) {
            showError("Unable to record result:\n" + ex.getMessage());
        }
    }

    private int extractPlayerId(String label) {
        String marker = "ID:";
        int idx = label.lastIndexOf(marker);
        if (idx < 0) {
            throw new IllegalArgumentException("Unable to parse player id from " + label);
        }
        int end = label.lastIndexOf(')');
        String raw = end > idx ? label.substring(idx + marker.length(), end) : label.substring(idx + marker.length());
        return Integer.parseInt(raw.trim());
    }

    private int parseScore(JTextField field, String label) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        int value = Integer.parseInt(text);
        if (value < 0) {
            throw new IllegalArgumentException(label + " must be zero or greater.");
        }
        return value;
    }

    private void clearForm() {
        matchCombo.setSelectedIndex(-1);
        clearMatchDetails();
        processedByField.setText("");
    }

    private void clearMatchDetails() {
        homeTeamLabel.setText("-");
        awayTeamLabel.setText("-");
        homeScoreField.setText("");
        awayScoreField.setText("");
        summaryField.setText("");
        playerModel.setRowCount(0);
        homeTeamInfo = null;
        awayTeamInfo = null;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
