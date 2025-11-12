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

public class MatchTeamManagerPanel extends JPanel {

    private final MatchTeamDAO matchTeamDAO = new MatchTeamDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<MatchTeam> cachedEntries = new ArrayList<>();

    private JComboBox<Match> matchCombo;
    private JComboBox<Team> teamCombo;
    private JCheckBox homeCheckBox;
    private JTextField scoreField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;

    private Integer selectedMatchId;
    private Integer selectedTeamId;

    public MatchTeamManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        initForm();
        initTable();
        initButtons();
        reloadMatches();
        reloadTeams();
        reloadTable();
    }

    private void initForm() {
        matchCombo = new JComboBox<>();
        matchCombo.setToolTipText("Match to link.");
        matchCombo.addActionListener(e -> reloadTable());

        teamCombo = new JComboBox<>();
        teamCombo.setToolTipText("Team participating in the match.");

        homeCheckBox = new JCheckBox("Home Team");

        scoreField = new JTextField("0");
        scoreField.setToolTipText("Score recorded for this team in the match.");

        add(buildFormPanel(), BorderLayout.NORTH);
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Match", "Team", "Home", "Score"}, 0
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
                if (row >= 0 && row < cachedEntries.size()) {
                    MatchTeam entry = cachedEntries.get(row);
                    populateForm(entry);
                }
            }
        });

        UAAPTheme.styleTable(table);
        table.getColumnModel().getColumn(1).setCellRenderer(new TeamLogoCellRenderer());

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear Form");
        refreshButton = new JButton("Refresh");

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        panel.add(refreshButton);

        add(panel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> handleAdd());
        updateButton.addActionListener(e -> handleUpdate());
        deleteButton.addActionListener(e -> handleDelete());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> {
            reloadMatches();
            reloadTeams();
            reloadTable();
        });
    }

    private void reloadMatches() {
        try {
            DefaultComboBoxModel<Match> model = new DefaultComboBoxModel<>();
            for (Match match : matchDAO.getAllMatches()) {
                model.addElement(match);
            }
            matchCombo.setModel(model);
            if (model.getSize() > 0) {
                matchCombo.setSelectedIndex(0);
            }
        } catch (SQLException ex) {
            showError("Error loading matches:\n" + ex.getMessage());
        }
        reloadTable();
    }

    private void reloadTeams() {
        try {
            DefaultComboBoxModel<Team> model = new DefaultComboBoxModel<>();
            for (Team team : teamDAO.getAllTeams()) {
                model.addElement(team);
            }
            teamCombo.setModel(model);
            if (model.getSize() > 0) {
                teamCombo.setSelectedIndex(0);
            }
        } catch (SQLException ex) {
            showError("Error loading teams:\n" + ex.getMessage());
        }
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        cachedEntries.clear();

        try {
            Match selectedMatch = (Match) matchCombo.getSelectedItem();
            if (selectedMatch != null) {
                cachedEntries = matchTeamDAO.getMatchTeamsForMatch(selectedMatch.getMatchId());
            } else {
                cachedEntries = matchTeamDAO.getAllMatchTeams();
            }
            for (MatchTeam entry : cachedEntries) {
                tableModel.addRow(new Object[]{
                        entry.getMatchLabel(),
                        entry.getTeamName(),
                        entry.isHomeTeam() ? "Yes" : "No",
                        entry.getTeamScore()
                });
            }
        } catch (SQLException ex) {
            showError("Error loading match teams:\n" + ex.getMessage());
        }
    }

    private void handleAdd() {
        try {
            MatchTeam entry = formToEntry();
            boolean wantsHome = entry.isHomeTeam();
            if (wantsHome) {
                entry.setHomeTeam(false);
            }
            matchTeamDAO.insertMatchTeam(entry);
            if (wantsHome) {
                matchTeamDAO.assignHomeTeam(entry.getMatchId(), entry.getTeamId());
            }
            showInfo("Entry saved.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to add entry:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedMatchId == null || selectedTeamId == null) {
            showError("Select an entry first.");
            return;
        }

        try {
            MatchTeam entry = formToEntry();
            boolean wantsHome = entry.isHomeTeam();
            if (wantsHome) {
                entry.setHomeTeam(false);
            }
            matchTeamDAO.updateMatchTeam(entry, selectedMatchId, selectedTeamId);
            if (wantsHome) {
                matchTeamDAO.assignHomeTeam(entry.getMatchId(), entry.getTeamId());
            }
            showInfo("Entry updated.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to update entry:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedMatchId == null || selectedTeamId == null) {
            showError("Select an entry first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected entry?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            matchTeamDAO.deleteMatchTeam(selectedMatchId, selectedTeamId);
            showInfo("Entry deleted.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to delete entry:\n" + ex.getMessage());
        }
    }

    private MatchTeam formToEntry() {
        if (matchCombo.getSelectedItem() == null || teamCombo.getSelectedItem() == null) {
            throw new IllegalArgumentException("Both match and team must be selected.");
        }

        Match match = (Match) matchCombo.getSelectedItem();
        Team team = (Team) teamCombo.getSelectedItem();

        int score = parseNonNegativeInt(scoreField, "Score");
        boolean isHome = homeCheckBox.isSelected();

        MatchTeam entry = new MatchTeam(match.getMatchId(), team.getTeamId(), isHome, score);
        entry.setMatchLabel(match.toString());
        entry.setTeamName(team.getTeamName());
        return entry;
    }

    private void populateForm(MatchTeam entry) {
        selectedMatchId = entry.getMatchId();
        selectedTeamId = entry.getTeamId();

        selectMatch(entry.getMatchId());
        selectTeam(entry.getTeamId());
        homeCheckBox.setSelected(entry.isHomeTeam());
        scoreField.setText(String.valueOf(entry.getTeamScore()));
    }

    private void selectMatch(int matchId) {
        ComboBoxModel<Match> model = matchCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Match match = model.getElementAt(i);
            if (match.getMatchId() == matchId) {
                matchCombo.setSelectedIndex(i);
                return;
            }
        }
        matchCombo.setSelectedIndex(-1);
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
        selectedMatchId = null;
        selectedTeamId = null;
        if (matchCombo.getItemCount() > 0) {
            matchCombo.setSelectedIndex(0);
        } else {
            matchCombo.setSelectedIndex(-1);
        }
        if (teamCombo.getItemCount() > 0) {
            teamCombo.setSelectedIndex(0);
        } else {
            teamCombo.setSelectedIndex(-1);
        }
        homeCheckBox.setSelected(false);
        scoreField.setText("0");
        table.clearSelection();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Match Team Participation"));

        addFormField(panel, 0, 0, "Match", matchCombo);
        addFormField(panel, 0, 1, "Team", teamCombo);
        addFormField(panel, 1, 0, "Score", scoreField);

        GridBagConstraints homeGbc = baseGbc(1, 2);
        homeGbc.anchor = GridBagConstraints.WEST;
        homeGbc.gridwidth = 2;
        panel.add(homeCheckBox, homeGbc);

        return panel;
    }

    private void addFormField(JPanel panel, int row, int col, String labelText, JComponent component) {
        GridBagConstraints labelGbc = baseGbc(row, col * 2);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(labelText), labelGbc);

        GridBagConstraints fieldGbc = baseGbc(row, col * 2 + 1);
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 0.5;
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

    private int parseNonNegativeInt(JTextField field, String label) {
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private static final class TeamLogoCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setIcon(UAAPAssets.teamLogo(value != null ? value.toString() : null, 42));
            label.setHorizontalTextPosition(JLabel.RIGHT);
            label.setIconTextGap(12);
            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? new java.awt.Color(226, 243, 232) : java.awt.Color.WHITE);
            }
            return label;
        }
    }
}
