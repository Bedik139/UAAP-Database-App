import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class TeamManagerPanel extends JPanel {

    private final TeamDAO teamDAO = new TeamDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private Integer selectedTeamId;
    private JComboBox<String> nameField;
    private JComboBox<String> sportField;
    private JTextField seasonsField;
    private JTextField winsField;
    private JTextField lossesField;
    private JTextField totalGamesField;

    private JComboBox<String> filterGenderCombo;
    private JComboBox<String> filterSportCombo;

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
        nameField = new JComboBox<>(TeamDAO.getAllowedTeamNames().toArray(new String[0]));
        nameField.setToolTipText("Select the official UAAP team name.");
        UAAPTheme.styleComboBox(nameField);
        
        sportField = new JComboBox<>(new String[]{"Basketball", "Volleyball"});
        sportField.setToolTipText("Select the sport (Basketball = Men, Volleyball = Women).");
        UAAPTheme.styleComboBox(sportField);
        
        seasonsField = new JTextField("0");
        seasonsField.setToolTipText("Total seasons the team has played.");
        UAAPTheme.styleTextField(seasonsField);
        
        winsField = new JTextField("0");
        winsField.setToolTipText("Wins recorded for the current season.");
        UAAPTheme.styleTextField(winsField);
        
        lossesField = new JTextField("0");
        lossesField.setToolTipText("Losses recorded for the current season.");
        UAAPTheme.styleTextField(lossesField);
        
        totalGamesField = new JTextField();
        totalGamesField.setEditable(false);
        totalGamesField.setToolTipText("Calculated automatically as wins + losses.");
        UAAPTheme.styleTextField(totalGamesField);

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
                new Object[]{"ID", "Name", "Gender", "Sport", "Seasons", "Wins", "Losses", "Total Games"}, 0
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
                if (!e.getValueIsAdjusting()) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        selectedTeamId = Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 0)));
                        nameField.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 1)));
                        sportField.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 3)));
                        seasonsField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
                        winsField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
                        lossesField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
                        totalGamesField.setText(String.valueOf(tableModel.getValueAt(row, 7)));
                    } else {
                        selectedTeamId = null;
                    }
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        UAAPTheme.elevate(tableScroll);
        add(tableScroll, BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        buttonPanel.setBackground(UAAPTheme.LIGHT_SURFACE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UAAPTheme.CARD_BORDER));
        
        addButton = new JButton("Add Team");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear Form");
        refreshButton = new JButton("Refresh");

        UAAPTheme.styleActionButton(addButton);
        UAAPTheme.styleActionButton(updateButton);
        UAAPTheme.styleDangerButton(deleteButton);
        UAAPTheme.styleNeutralButton(clearButton);
        UAAPTheme.styleInfoButton(refreshButton);

        addButton.addActionListener(e -> handleAdd());
        updateButton.addActionListener(e -> handleUpdate());
        deleteButton.addActionListener(e -> handleDelete());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> reloadTable());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

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
                        team.getGender(),
                        team.getSport(),
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
        if (selectedTeamId == null) {
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
        if (selectedTeamId == null) {
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
            teamDAO.deleteTeam(selectedTeamId);
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
        
        String sport = (String) sportField.getSelectedItem();
        if (sport == null || sport.trim().isEmpty()) {
            throw new IllegalArgumentException("Sport is required.");
        }

        // Automatically determine gender based on sport
        String gender;
        if (sport.equalsIgnoreCase("Basketball")) {
            gender = "Men";
        } else if (sport.equalsIgnoreCase("Volleyball")) {
            gender = "Women";
            // Transform team name to use "Lady" prefix for volleyball
            name = transformToLadyTeamName(name);
        } else {
            throw new IllegalArgumentException("Invalid sport selected.");
        }

        int seasons = parseNonNegativeInt(seasonsField, "Seasons played");
        int wins = parseNonNegativeInt(winsField, "Wins");
        int losses = parseNonNegativeInt(lossesField, "Losses");
        int total = computeTotalGames(wins, losses);

        Team team = new Team(name, gender, sport, seasons, wins, losses, total);
        if (includeId) {
            team.setTeamId(selectedTeamId);
        }
        team.setTotalGamesPlayed(total);
        return team;
    }

    private String transformToLadyTeamName(String originalName) {
        // Transform team names to "Lady" version for volleyball
        // e.g., "De La Salle Green Archers" -> "De La Salle Lady Archers"
        if (originalName.contains("Green Archers")) {
            return originalName.replace("Green Archers", "Lady Archers");
        } else if (originalName.contains("Blue Eagles")) {
            return originalName.replace("Blue Eagles", "Lady Eagles");
        } else if (originalName.contains("Fighting Maroons")) {
            return originalName.replace("Fighting Maroons", "Lady Maroons");
        } else if (originalName.contains("Growling Tigers")) {
            return originalName.replace("Growling Tigers", "Lady Tigers");
        } else if (originalName.contains("Tamaraws")) {
            return originalName.replace("Tamaraws", "Lady Tamaraws");
        } else if (originalName.contains("Red Warriors")) {
            return originalName.replace("Red Warriors", "Lady Warriors");
        } else if (originalName.contains("Bulldogs")) {
            return originalName.replace("Bulldogs", "Lady Bulldogs");
        } else if (originalName.contains("Soaring Falcons")) {
            return originalName.replace("Soaring Falcons", "Lady Falcons");
        }
        
        // If no match, return original name
        return originalName;
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
        selectedTeamId = null;
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
        panel.setBackground(UAAPTheme.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UAAPTheme.CARD_BORDER, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("Team Details");
        titleLabel.setFont(new java.awt.Font("Segoe UI Semibold", java.awt.Font.BOLD, 14));
        titleLabel.setForeground(UAAPTheme.TEXT_PRIMARY);
        
        GridBagConstraints titleGbc = new GridBagConstraints();
        titleGbc.gridx = 0;
        titleGbc.gridy = 0;
        titleGbc.gridwidth = 4;
        titleGbc.anchor = GridBagConstraints.WEST;
        titleGbc.insets = new Insets(0, 0, 8, 0);
        titleGbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(titleLabel, titleGbc);

        addFormField(panel, 1, 0, "Team Name", nameField);
        addFormField(panel, 1, 1, "Sport", sportField);
        addFormField(panel, 2, 0, "Seasons Played", seasonsField);
        addFormField(panel, 2, 1, "Wins", winsField);
        addFormField(panel, 3, 0, "Losses", lossesField);

        return panel;
    }

    private void addFormField(JPanel panel, int row, int col, String labelText, JComponent component) {
        JLabel jLabel = new JLabel(labelText + ":");
        jLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jLabel.setForeground(UAAPTheme.TEXT_PRIMARY);
        
        GridBagConstraints labelGbc = baseGbc(row, col * 2);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(jLabel, labelGbc);

        GridBagConstraints fieldGbc = baseGbc(row, col * 2 + 1);
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 0.5;
        panel.add(component, fieldGbc);
    }

    private GridBagConstraints baseGbc(int row, int col) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.insets = new Insets(4, 8, 4, 8);
        return gbc;
    }
}
