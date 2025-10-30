import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatchSetScoreManagerPanel extends JPanel {

    private final MatchSetScoreDAO scoreDAO = new MatchSetScoreDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final MatchTeamDAO matchTeamDAO = new MatchTeamDAO();

    private JTextField matchIdField;
    private JButton loadButton;
    private JLabel matchInfoLabel;

    private JTable table;
    private DefaultTableModel tableModel;
    private List<MatchSetScore> cachedScores = new ArrayList<>();

    private JComboBox<Team> teamCombo;
    private JTextField setField;
    private JTextField pointsField;

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

        loadButton = new JButton("Load Match");
        loadButton.addActionListener(e -> handleLoadMatch());

        matchInfoLabel = new JLabel("No match loaded.");

        panel.add(new JLabel("Match ID:"));
        panel.add(matchIdField);
        panel.add(loadButton);
        panel.add(Box.createHorizontalStrut(16));
        panel.add(matchInfoLabel);

        add(panel, BorderLayout.NORTH);
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

        setField = new JTextField();
        setField.setToolTipText("Set number (1-5).");

        pointsField = new JTextField();
        pointsField.setToolTipText("Points scored in the set.");

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

            currentMatchId = matchId;
            matchInfoLabel.setText(match.toString());

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
        try {
            for (MatchTeam mt : matchTeamDAO.getMatchTeamsForMatch(currentMatchId)) {
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
    }

    private void reloadScores() {
        tableModel.setRowCount(0);
        cachedScores.clear();

        try {
            cachedScores = scoreDAO.getScoresForMatch(currentMatchId);
            for (MatchSetScore score : cachedScores) {
                tableModel.addRow(new Object[]{
                        score.getTeamName(),
                        score.getSetNo(),
                        score.getPoints()
                });
            }
        } catch (SQLException ex) {
            showError("Error loading scores:\n" + ex.getMessage());
        }
    }

    private void handleAdd() {
        if (!isMatchLoaded()) return;

        try {
            MatchSetScore score = formToScore();
            scoreDAO.insertScore(score);
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
        pointsField.setText(String.valueOf(score.getPoints()));
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
}
