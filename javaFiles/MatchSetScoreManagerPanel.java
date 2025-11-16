import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MatchSetScoreManagerPanel extends JPanel {

    private final MatchSetScoreDAO scoreDAO = new MatchSetScoreDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final MatchTeamDAO matchTeamDAO = new MatchTeamDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final ScoreAggregationService scoreAggregationService = new ScoreAggregationService();

    private JTextField matchIdField;
    private JButton loadButton;
    private JLabel matchInfoLabel;

    private JTable table;
    private DefaultTableModel tableModel;
    private List<MatchSetScore> cachedScores = new ArrayList<>();
    private List<MatchTeam> loadedTeams = new ArrayList<>();

    private JComboBox<Team> teamCombo;
    private JTextField setField;
    private JTextField pointsField;
    private JTextArea setSummaryArea;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    private Integer currentMatchId;
    private Integer selectedTeamId;
    private Integer selectedSetNo;

    public MatchSetScoreManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        initHeader();
        initTable();
        initForm();
        initButtons();
    }

    private void initHeader() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        matchIdField = new JTextField(8);
        matchIdField.setToolTipText("Enter a volleyball match ID to load set scores.");
        UAAPTheme.styleTextField(matchIdField);

        loadButton = new JButton("Load Match");
        UAAPTheme.styleInfoButton(loadButton);
        loadButton.addActionListener(e -> handleLoadMatch());

        matchInfoLabel = new JLabel("No match loaded.");

        panel.add(new JLabel("Match ID:"));
        panel.add(matchIdField);
        panel.add(loadButton);
        panel.add(Box.createHorizontalStrut(16));
        panel.add(matchInfoLabel);

        setSummaryArea = new JTextArea(4, 40);
        setSummaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        setSummaryArea.setEditable(false);
        setSummaryArea.setLineWrap(true);
        setSummaryArea.setWrapStyleWord(true);
        setSummaryArea.setText("Load a match to view set-by-set scoring.");
        JScrollPane summaryScroll = new JScrollPane(setSummaryArea);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Set Breakdown"));

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.NORTH);
        container.add(summaryScroll, BorderLayout.SOUTH);

        add(container, BorderLayout.NORTH);
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Team", "Set", "Points"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UAAPTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < cachedScores.size()) {
                    MatchSetScore score = cachedScores.get(row);
                    populateForm(score);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void initForm() {
        teamCombo = new JComboBox<>();
        teamCombo.setEnabled(false);
        teamCombo.setToolTipText("Participating team for the loaded match.");
        UAAPTheme.styleComboBox(teamCombo);

        setField = new JTextField();
        setField.setToolTipText("Set number (1-5).");
        UAAPTheme.styleTextField(setField);

        pointsField = new JTextField();
        pointsField.setToolTipText("Points scored in the set.");
        UAAPTheme.styleTextField(pointsField);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Set Score Details"));

        addFormField(formPanel, 0, "Team", teamCombo);
        addFormField(formPanel, 1, "Set No.", setField);
        addFormField(formPanel, 2, "Points", pointsField);

        add(formPanel, BorderLayout.EAST);
    }

    private void initButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear Form");

        UAAPTheme.styleActionButton(addButton);
        UAAPTheme.styleActionButton(updateButton);
        UAAPTheme.styleDangerButton(deleteButton);
        UAAPTheme.styleNeutralButton(clearButton);

        addButton.addActionListener(e -> handleAdd());
        updateButton.addActionListener(e -> handleUpdate());
        deleteButton.addActionListener(e -> handleDelete());
        clearButton.addActionListener(e -> clearForm());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);

        add(panel, BorderLayout.SOUTH);

        setFormEnabled(false);
    }

    private void handleLoadMatch() {
        String text = matchIdField.getText().trim();
        if (text.isEmpty()) {
            showError("Enter a match ID.");
            return;
        }

        try {
            int matchId = Integer.parseInt(text);
            Match match = matchDAO.getMatchById(matchId);
            if (match == null) {
                showError("Match not found.");
                return;
            }

            Event event = eventDAO.getEventById(match.getEventId());
            if (event == null) {
                showError("Unable to locate the parent event for this match.");
                resetPanelState("Event metadata missing.");
                return;
            }

            if (!isVolleyball(event.getSport())) {
                showError("Set scores are reserved for volleyball matches. Loaded sport: "
                        + safeSportLabel(event.getSport()));
                resetPanelState("Unsupported sport: " + safeSportLabel(event.getSport()));
                return;
            }

            currentMatchId = matchId;
            event.getSport();
            matchInfoLabel.setText(match.toString() + " • " + safeSportLabel(event.getSport()));

            seedSetDataIfNeeded(match);
            recomputeTotals();
            reloadTeamsForMatch();
            reloadScores();
            clearForm();
            setFormEnabled(true);
        } catch (NumberFormatException ex) {
            showError("Match ID must be a number.");
        } catch (SQLException ex) {
            showError("Error loading match:\n" + ex.getMessage());
        }
    }

    private void reloadTeamsForMatch() {
        DefaultComboBoxModel<Team> model = new DefaultComboBoxModel<>();
        loadedTeams = new ArrayList<>();
        if (currentMatchId == null) {
            teamCombo.setModel(model);
            teamCombo.setEnabled(false);
            updateSetSummaryArea();
            return;
        }
        try {
            loadedTeams = matchTeamDAO.getMatchTeamsForMatch(currentMatchId);
            for (MatchTeam mt : loadedTeams) {
                Team team = new Team();
                team.setTeamId(mt.getTeamId());
                team.setTeamName(mt.getTeamName());
                model.addElement(team);
            }
        } catch (SQLException ex) {
            showError("Error loading teams for match:\n" + ex.getMessage());
        }
        teamCombo.setModel(model);
        teamCombo.setEnabled(model.getSize() > 0);
        if (model.getSize() > 0) {
            teamCombo.setSelectedIndex(0);
        }
        updateSetSummaryArea();
    }

    private void reloadScores() {
        tableModel.setRowCount(0);
        cachedScores.clear();

        if (currentMatchId == null) {
            updateSetSummaryArea();
            return;
        }
        try {
            cachedScores = scoreDAO.getScoresForMatch(currentMatchId);
            for (MatchSetScore score : cachedScores) {
                tableModel.addRow(new Object[]{
                        score.getTeamName(),
                        score.getSetNo(),
                        score.getSetPoints()
                });
            }
        } catch (SQLException ex) {
            showError("Error loading scores:\n" + ex.getMessage());
        }
        updateSetSummaryArea();
    }

    private void handleAdd() {
        if (!isMatchLoaded()) return;

        try {
            MatchSetScore score = formToScore();
            scoreDAO.insertScore(score);
            recomputeTotals();
            showInfo("Set score saved.");
            reloadScores();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to add set score:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (!isMatchLoaded()) return;
        if (selectedTeamId == null || selectedSetNo == null) {
            showError("Select an entry first.");
            return;
        }

        try {
            MatchSetScore score = formToScore();
            scoreDAO.updateScore(score, currentMatchId, selectedTeamId, selectedSetNo);
            recomputeTotals();
            showInfo("Set score updated.");
            reloadScores();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to update set score:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (!isMatchLoaded()) return;
        if (selectedTeamId == null || selectedSetNo == null) {
            showError("Select an entry first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected set score?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            scoreDAO.deleteScore(currentMatchId, selectedTeamId, selectedSetNo);
            recomputeTotals();
            showInfo("Set score deleted.");
            reloadScores();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to delete set score:\n" + ex.getMessage());
        }
    }

    private MatchSetScore formToScore() {
        if (teamCombo.getSelectedItem() == null) {
            throw new IllegalArgumentException("Select a team.");
        }

        Team team = (Team) teamCombo.getSelectedItem();
        int setNo = parseSet();
        int points = parseNonNegative(pointsField, "Points");

        MatchSetScore score = new MatchSetScore(currentMatchId, team.getTeamId(), setNo, points);
        score.setTeamName(team.getTeamName());
        return score;
    }

    private int parseSet() {
        int setNo = parseNonNegative(setField, "Set number");
        if (setNo < 1 || setNo > 5) {
            throw new IllegalArgumentException("Set number must be between 1 and 5.");
        }
        return setNo;
    }

    private int parseNonNegative(JTextField field, String label) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            int value = Integer.parseInt(text);
            if (value < 0) {
                throw new IllegalArgumentException(label + " must be zero or greater.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private void populateForm(MatchSetScore score) {
        selectedTeamId = score.getTeamId();
        selectedSetNo = score.getSetNo();

        selectTeam(score.getTeamId());
        setField.setText(String.valueOf(score.getSetNo()));
        pointsField.setText(String.valueOf(score.getSetPoints()));
    }

    private void selectTeam(int teamId) {
        ComboBoxModel<Team> model = teamCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Team team = model.getElementAt(i);
            if (team.getTeamId() == teamId) {
                teamCombo.setSelectedIndex(i);
                return;
            }
        }
        teamCombo.setSelectedIndex(-1);
    }

    private void clearForm() {
        selectedTeamId = null;
        selectedSetNo = null;
        if (teamCombo.isEnabled() && teamCombo.getItemCount() > 0) {
            teamCombo.setSelectedIndex(0);
        } else {
            teamCombo.setSelectedIndex(-1);
        }
        setField.setText("");
        pointsField.setText("");
        table.clearSelection();
    }

    private boolean isMatchLoaded() {
        if (currentMatchId == null) {
            showError("Load a match first.");
            return false;
        }
        return true;
    }

    private void setFormEnabled(boolean enabled) {
        teamCombo.setEnabled(enabled && teamCombo.getItemCount() > 0);
        setField.setEnabled(enabled);
        pointsField.setEnabled(enabled);
        addButton.setEnabled(enabled);
        updateButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }

    private void recomputeTotals() {
        if (currentMatchId == null) {
            return;
        }
        try {
            scoreAggregationService.updateTotalsForMatch(currentMatchId);
        } catch (SQLException ex) {
            showError("Unable to recompute match totals:\n" + ex.getMessage());
        }
    }

    private void resetPanelState(String label) {
        currentMatchId = null;
        matchInfoLabel.setText(label);
        tableModel.setRowCount(0);
        cachedScores.clear();
        teamCombo.setModel(new DefaultComboBoxModel<>());
        loadedTeams = new ArrayList<>();
        updateSetSummaryArea();
        setFormEnabled(false);
    }

    private boolean isVolleyball(String sport) {
        return sport != null && sport.toLowerCase().contains("volley");
    }

    private String safeSportLabel(String sport) {
        return sport == null || sport.isBlank() ? "Unknown Sport" : sport;
    }

    private void updateSetSummaryArea() {
        if (setSummaryArea == null) {
            return;
        }
        if (currentMatchId == null) {
            setSummaryArea.setText("Load a match to view set-by-set scoring.");
            setSummaryArea.setCaretPosition(0);
            return;
        }
        if (loadedTeams.isEmpty()) {
            setSummaryArea.setText("Assign participating teams to this match to see the breakdown.");
            setSummaryArea.setCaretPosition(0);
            return;
        }
        if (cachedScores.isEmpty()) {
            setSummaryArea.setText("No set scores recorded yet.");
            setSummaryArea.setCaretPosition(0);
            return;
        }

        List<MatchTeam> orderedTeams = new ArrayList<>(loadedTeams);
        orderedTeams.sort(Comparator.comparing((MatchTeam team) -> !team.isHomeTeam()));

        Map<Integer, Map<Integer, Integer>> perSet = new TreeMap<>();
        for (MatchSetScore score : cachedScores) {
            perSet
                    .computeIfAbsent(score.getSetNo(), key -> new TreeMap<>())
                    .put(score.getTeamId(), score.getSetPoints());
        }

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%-10s", "Set"));
        for (MatchTeam team : orderedTeams) {
            builder.append(String.format("%-18s", team.getTeamName()));
        }
        builder.append(System.lineSeparator());

        for (Map.Entry<Integer, Map<Integer, Integer>> entry : perSet.entrySet()) {
            builder.append(String.format("%-10s", "Set " + entry.getKey()));
            Map<Integer, Integer> pointsByTeam = entry.getValue();
            for (MatchTeam team : orderedTeams) {
                int points = pointsByTeam.getOrDefault(team.getTeamId(), 0);
                builder.append(String.format("%-18d", points));
            }
            builder.append(System.lineSeparator());
        }

        setSummaryArea.setText(builder.toString());
        setSummaryArea.setCaretPosition(0);
    }

    private void addFormField(JPanel panel, int row, String labelText, JComponent component) {
        GridBagConstraints labelGbc = baseGbc(row, 0);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(labelText), labelGbc);

        GridBagConstraints fieldGbc = baseGbc(row, 1);
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1.0;
        panel.add(component, fieldGbc);
    }

    private GridBagConstraints baseGbc(int row, int col) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weighty = 0;
        return gbc;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void seedSetDataIfNeeded(Match match) {
        if (!"Completed".equalsIgnoreCase(match.getStatus())) {
            return;
        }
        try {
            if (!scoreDAO.getScoresForMatch(match.getMatchId()).isEmpty()) {
                return;
            }
            List<MatchTeam> teams = matchTeamDAO.getMatchTeamsForMatch(match.getMatchId());
            if (teams.isEmpty()) {
                return;
            }
            final int segments = 5;
            for (MatchTeam team : teams) {
                distributeSetPoints(match.getMatchId(), team, segments);
            }
        } catch (SQLException ex) {
            showError("Unable to seed set data:\n" + ex.getMessage());
        }
    }

    private void distributeSetPoints(int matchId, MatchTeam team, int segments) throws SQLException {
        int total = Math.max(0, team.getTeamScore());
        int base = total / segments;
        int remainder = total % segments;
        for (int setNo = 1; setNo <= segments; setNo++) {
            int points = base + (setNo <= remainder ? 1 : 0);
            MatchSetScore score = new MatchSetScore(matchId, team.getTeamId(), setNo, points, null, team.getTeamName());
            scoreDAO.insertScore(score);
        }
    }
}
