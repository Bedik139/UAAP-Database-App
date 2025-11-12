import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

public class TeamManagerPanel extends JPanel {

    private final TeamDAO teamDAO = new TeamDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField idField;
    private JComboBox<String> nameField;
    private JTextField seasonsField;
    private JTextField winsField;
    private JTextField lossesField;
    private JTextField totalGamesField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;

    public TeamManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        initForm();
        initTable();
        initButtons();
        reloadTable();
    }

    private void initForm() {
        idField = new JTextField();
        idField.setEditable(false);
        idField.setToolTipText("Auto-filled when you select a team.");
        nameField = new JComboBox<>(TeamDAO.getAllowedTeamNames().toArray(new String[0]));
        nameField.setToolTipText("Select the official UAAP team name.");
        seasonsField = new JTextField("0");
        seasonsField.setToolTipText("Total seasons the team has played.");
        winsField = new JTextField("0");
        winsField.setToolTipText("Wins recorded for the current season.");
        lossesField = new JTextField("0");
        lossesField.setToolTipText("Losses recorded for the current season.");
        totalGamesField = new JTextField();
        totalGamesField.setEditable(false);
        totalGamesField.setToolTipText("Calculated automatically as wins + losses.");

        JPanel formPanel = buildFormPanel();
        add(formPanel, BorderLayout.NORTH);

        DocumentListener totalListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateTotalGamesField(); }
            @Override public void removeUpdate(DocumentEvent e) { updateTotalGamesField(); }
            @Override public void changedUpdate(DocumentEvent e) { updateTotalGamesField(); }
        };
        winsField.getDocument().addDocumentListener(totalListener);
        lossesField.getDocument().addDocumentListener(totalListener);
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Seasons", "Wins", "Losses", "Total Games"}, 0
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
                if (row >= 0) {
                    idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
                    nameField.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 1)));
                    seasonsField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
                    winsField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
                    lossesField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
                    totalGamesField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear Form");
        refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        addButton.addActionListener(e -> handleAdd());
        updateButton.addActionListener(e -> handleUpdate());
        deleteButton.addActionListener(e -> handleDelete());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> reloadTable());

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        try {
            List<Team> teams = teamDAO.getAllTeams();
            for (Team team : teams) {
                tableModel.addRow(new Object[]{
                        team.getTeamId(),
                        team.getTeamName(),
                        team.getSeasonsPlayed(),
                        team.getStandingWins(),
                        team.getStandingLosses(),
                        team.getTotalGamesPlayed()
                });
            }
        } catch (SQLException ex) {
            showError("Error loading teams:\n" + ex.getMessage());
        }
    }

    private void handleAdd() {
        try {
            Team team = formToTeam(false);
            teamDAO.insertTeam(team);
            showInfo("Team added.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Error adding team:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (idField.getText().trim().isEmpty()) {
            showError("Select a row first.");
            return;
        }

        try {
            Team team = formToTeam(true);
            teamDAO.updateTeam(team);
            showInfo("Team updated.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Error updating team:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (idField.getText().trim().isEmpty()) {
            showError("Select a row first.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Delete selected team?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int teamId = Integer.parseInt(idField.getText().trim());
            teamDAO.deleteTeam(teamId);
            showInfo("Team deleted.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Error deleting team:\n" + ex.getMessage());
        }
    }

    private Team formToTeam(boolean includeId) {
        String name = (String) nameField.getSelectedItem();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name is required.");
        }
        name = name.trim();

        int seasons = parseNonNegativeInt(seasonsField, "Seasons played");
        int wins = parseNonNegativeInt(winsField, "Wins");
        int losses = parseNonNegativeInt(lossesField, "Losses");
        int total = computeTotalGames(wins, losses);

        Team team = new Team(name, seasons, wins, losses, total);
        if (includeId) {
            int id = Integer.parseInt(idField.getText().trim());
            team.setTeamId(id);
        }
        team.setTotalGamesPlayed(total);
        return team;
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

    private int computeTotalGames(int wins, int losses) {
        if (wins < 0 || losses < 0) {
            throw new IllegalArgumentException("Wins and losses must be zero or greater.");
        }
        return wins + losses;
    }

    private void updateTotalGamesField() {
        try {
            int wins = Integer.parseInt(winsField.getText().trim());
            int losses = Integer.parseInt(lossesField.getText().trim());
            if (wins >= 0 && losses >= 0) {
                totalGamesField.setText(String.valueOf(wins + losses));
            } else {
                totalGamesField.setText("");
            }
        } catch (NumberFormatException ex) {
            totalGamesField.setText("");
        }
    }

    private void clearForm() {
        idField.setText("");
        if (nameField.getItemCount() > 0) {
            nameField.setSelectedIndex(0);
        }
        seasonsField.setText("0");
        winsField.setText("0");
        lossesField.setText("0");
        totalGamesField.setText("");
        table.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Team Details"));

        addFormField(panel, 0, 0, "Team ID (auto)", idField);
        addFormField(panel, 0, 1, "Team Name", nameField);
        addFormField(panel, 1, 0, "Seasons Played", seasonsField);
        addFormField(panel, 1, 1, "Total Games (auto)", totalGamesField);
        addFormField(panel, 2, 0, "Wins", winsField);
        addFormField(panel, 2, 1, "Losses", lossesField);

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
}
