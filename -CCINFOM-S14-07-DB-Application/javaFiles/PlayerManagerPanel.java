import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PlayerManagerPanel extends JPanel {

    private static final String[] BASKETBALL_POSITIONS = {
            "Select Position",
            "Point Guard (PG)",
            "Shooting Guard (SG)",
            "Small Forward (SF)",
            "Power Forward (PF)",
            "Center (C)"
    };

    private static final String[] VOLLEYBALL_POSITIONS = {
            "Select Position",
            "Setter (S)",
            "Outside Hitter (OH)",
            "Opposite Spiker (OS)",
            "Middle Blocker (MB)",
            "Libero (L)"
    };

    private final PlayerDAO playerDAO = new PlayerDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Player> cachedPlayers = new ArrayList<>();

    private JTabbedPane sportTabs;
    private JPanel basketballPanel;
    private JPanel volleyballPanel;

    // Basketball panel components
    private JTable basketballTable;
    private DefaultTableModel basketballTableModel;
    private List<Player> cachedBasketballPlayers = new ArrayList<>();
    
    // Volleyball panel components
    private JTable volleyballTable;
    private DefaultTableModel volleyballTableModel;
    private List<Player> cachedVolleyballPlayers = new ArrayList<>();

    private Integer selectedPlayerId;
    private JComboBox<Team> teamCombo;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField numberField;
    private JComboBox<String> sportCombo;
    private JTextField ageField;
    private JComboBox<String> positionCombo;
    private JTextField weightField;
    private JTextField heightField;
    private JTextField scoreField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;

    public PlayerManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        initForm();
        initSportTabs();
        initButtons();
        reloadTeams();
        reloadAllTables();
    }

    private void initForm() {
        teamCombo = new JComboBox<>();
        teamCombo.setToolTipText("Pick the team this player belongs to.");
        UAAPTheme.styleComboBox(teamCombo);
        
        firstNameField = new JTextField();
        firstNameField.setToolTipText("Enter the player's given name.");
        UAAPTheme.styleTextField(firstNameField);
        
        lastNameField = new JTextField();
        lastNameField.setToolTipText("Enter the player's surname.");
        UAAPTheme.styleTextField(lastNameField);
        
        numberField = new JTextField();
        numberField.setToolTipText("Jersey number. Must be unique per team.");
        UAAPTheme.styleTextField(numberField);
        
        sportCombo = new JComboBox<>(new String[]{"Basketball", "Volleyball"});
        sportCombo.setToolTipText("Select the sport for this player.");
        sportCombo.addActionListener(e -> updatePositionOptions());
        UAAPTheme.styleComboBox(sportCombo);
        
        ageField = new JTextField();
        ageField.setToolTipText("Optional. Enter age in years.");
        UAAPTheme.styleTextField(ageField);
        
        positionCombo = new JComboBox<>(BASKETBALL_POSITIONS);
        positionCombo.setToolTipText("Select the player's position.");
        UAAPTheme.styleComboBox(positionCombo);
        
        weightField = new JTextField();
        weightField.setToolTipText("Optional. Weight in kilograms (decimal allowed).");
        UAAPTheme.styleTextField(weightField);
        
        heightField = new JTextField();
        heightField.setToolTipText("Optional. Height in meters (decimal allowed).");
        UAAPTheme.styleTextField(heightField);
        
        scoreField = new JTextField("0");
        scoreField.setToolTipText("Running total score for this player.");
        UAAPTheme.styleTextField(scoreField);

        JPanel formPanel = buildFormPanel();
        add(formPanel, BorderLayout.NORTH);
    }

    private void updatePositionOptions() {
        String selectedSport = (String) sportCombo.getSelectedItem();
        String[] positions = "Basketball".equals(selectedSport) ? BASKETBALL_POSITIONS : VOLLEYBALL_POSITIONS;
        positionCombo.setModel(new DefaultComboBoxModel<>(positions));
    }

    private void initSportTabs() {
        sportTabs = new JTabbedPane();
        UAAPTheme.styleTabPane(sportTabs);

        // Basketball Tab
        basketballPanel = createSportPanel("Basketball");
        sportTabs.addTab(" Basketball", basketballPanel);

        // Volleyball Tab
        volleyballPanel = createSportPanel("Volleyball");
        sportTabs.addTab(" Volleyball", volleyballPanel);

        add(sportTabs, BorderLayout.CENTER);
    }

    private JPanel createSportPanel(String sport) {
        JPanel panel = new JPanel(new BorderLayout());
        
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Team", "First Name", "Last Name", " Jersey Number",
                        "Age", "Position", "Weight (kg)", "Height (m)", "Score"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        UAAPTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        if ("Basketball".equals(sport)) {
            basketballTable = table;
            basketballTableModel = model;
            table.getSelectionModel().addListSelectionListener(e -> {
                int row = basketballTable.getSelectedRow();
                if (row >= 0 && row < cachedBasketballPlayers.size()) {
                    Player player = cachedBasketballPlayers.get(row);
                    populateForm(player);
                }
            });
        } else {
            volleyballTable = table;
            volleyballTableModel = model;
            table.getSelectionModel().addListSelectionListener(e -> {
                int row = volleyballTable.getSelectedRow();
                if (row >= 0 && row < cachedVolleyballPlayers.size()) {
                    Player player = cachedVolleyballPlayers.get(row);
                    populateForm(player);
                }
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void initButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        buttonPanel.setBackground(UAAPTheme.LIGHT_SURFACE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UAAPTheme.CARD_BORDER));
        
        addButton = new JButton("Add Player");
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
        refreshButton.addActionListener(e -> {
            reloadTeams();
            reloadAllTables();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void reloadTeams() {
        try {
            List<Team> teams = teamDAO.getAllTeams();
            DefaultComboBoxModel<Team> model = new DefaultComboBoxModel<>();
            for (Team team : teams) {
                model.addElement(team);
            }
            teamCombo.setModel(model);
            if (teams.isEmpty()) {
                teamCombo.setSelectedIndex(-1);
            } else {
                teamCombo.setSelectedIndex(0);
            }
        } catch (SQLException ex) {
            showError("Error loading teams:\n" + ex.getMessage());
        }
    }

    private void reloadAllTables() {
        reloadTableBySport("Basketball");
        reloadTableBySport("Volleyball");
    }

    private void reloadTableBySport(String sport) {
        try {
            List<Player> players = playerDAO.getPlayersBySport(sport);
            
            if ("Basketball".equals(sport)) {
                basketballTableModel.setRowCount(0);
                cachedBasketballPlayers.clear();
                cachedBasketballPlayers.addAll(players);
                for (Player player : players) {
                    basketballTableModel.addRow(createTableRow(player));
                }
            } else {
                volleyballTableModel.setRowCount(0);
                cachedVolleyballPlayers.clear();
                cachedVolleyballPlayers.addAll(players);
                for (Player player : players) {
                    volleyballTableModel.addRow(createTableRow(player));
                }
            }
        } catch (SQLException ex) {
            showError("Error loading " + sport + " players:\n" + ex.getMessage());
        }
    }

    private Object[] createTableRow(Player player) {
        return new Object[]{
                player.getPlayerId(),
                player.getTeamName(),
                player.getFirstName(),
                player.getLastName(),
                player.getPlayerNumber(),
                player.getAge() != null ? player.getAge() : "",
                player.getPosition() != null ? player.getPosition() : "",
                player.getWeight() != null ? player.getWeight().toPlainString() : "",
                player.getHeight() != null ? player.getHeight().toPlainString() : "",
                player.getIndividualScore()
        };
    }

    private void handleAdd() {
        try {
            Player player = formToPlayer(false);
            playerDAO.insertPlayer(player);
            showInfo("Player added.");
            reloadAllTables();
            clearForm();
        } catch (Exception ex) {
            showError("Error adding player:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (selectedPlayerId == null) {
            showError("Select a row first.");
            return;
        }

        try {
            Player player = formToPlayer(true);
            playerDAO.updatePlayer(player);
            showInfo("Player updated.");
            reloadAllTables();
            clearForm();
        } catch (Exception ex) {
            showError("Error updating player:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedPlayerId == null) {
            showError("Select a row first.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Delete selected player?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            playerDAO.deletePlayer(selectedPlayerId);
            showInfo("Player deleted.");
            reloadAllTables();
            clearForm();
        } catch (Exception ex) {
            showError("Error deleting player:\n" + ex.getMessage());
        }
    }

    private Player formToPlayer(boolean includeId) {
        Team selectedTeam = (Team) teamCombo.getSelectedItem();
        if (selectedTeam == null) {
            throw new IllegalStateException("Add a team first before creating players.");
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new IllegalArgumentException("First and last name are required.");
        }

        int playerNumber = parseNonNegativeInt(numberField, "Player number");
        String sport = (String) sportCombo.getSelectedItem();
        if (sport == null || sport.trim().isEmpty()) {
            throw new IllegalArgumentException("Sport is required.");
        }
        
        // Validate gender-sport compatibility
        String teamGender = selectedTeam.getGender();
        if ("Men".equalsIgnoreCase(teamGender) && !"Basketball".equalsIgnoreCase(sport)) {
            throw new IllegalArgumentException(
                "Sport Assignment Restriction:\n\n" +
                "Men's teams are currently limited to Basketball players only.\n\n" +
                "The application currently supports Men's Basketball and Women's Volleyball. " +
                "Additional sports are under development and will be available in future updates."
            );
        }
        if ("Women".equalsIgnoreCase(teamGender) && !"Volleyball".equalsIgnoreCase(sport)) {
            throw new IllegalArgumentException(
                "Sport Assignment Restriction:\n\n" +
                "Women's teams are currently limited to Volleyball players only.\n\n" +
                "The application currently supports Men's Basketball and Women's Volleyball. " +
                "Additional sports are under development and will be available in future updates."
            );
        }
        Integer age = parseOptionalNonNegativeInt(ageField, "Age");
        String position = (String) positionCombo.getSelectedItem();
        if (position != null && (position.trim().isEmpty() || position.equals("Select Position"))) {
            position = null;
        }
        BigDecimal weight = parseOptionalDecimal(weightField, "Weight");
        BigDecimal height = parseOptionalDecimal(heightField, "Height");
        int score = parseNonNegativeInt(scoreField, "Individual score");

        Player player = new Player(
                selectedTeam.getTeamId(),
                firstName,
                lastName,
                playerNumber,
                sport,
                age,
                position,
                weight,
                height,
                score
        );
        player.setTeamName(selectedTeam.getTeamName());

        if (includeId) {
            player.setPlayerId(selectedPlayerId);
        }

        return player;
    }

    private void populateForm(Player player) {
        selectedPlayerId = player.getPlayerId();
        selectTeamInCombo(player.getTeamId());
        firstNameField.setText(player.getFirstName());
        lastNameField.setText(player.getLastName());
        numberField.setText(String.valueOf(player.getPlayerNumber()));
        sportCombo.setSelectedItem(player.getSport());
        updatePositionOptions();
        ageField.setText(player.getAge() != null ? String.valueOf(player.getAge()) : "");
        selectPositionInCombo(player.getPosition());
        weightField.setText(player.getWeight() != null ? player.getWeight().toPlainString() : "");
        heightField.setText(player.getHeight() != null ? player.getHeight().toPlainString() : "");
        scoreField.setText(String.valueOf(player.getIndividualScore()));
    }

    private void selectTeamInCombo(int teamId) {
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

    private void selectPositionInCombo(String position) {
        if (position == null || position.isBlank()) {
            positionCombo.setSelectedIndex(0);
            return;
        }
        ComboBoxModel<String> model = positionCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            String option = model.getElementAt(i);
            if (option.equalsIgnoreCase(position)) {
                positionCombo.setSelectedIndex(i);
                return;
            }
        }
        positionCombo.addItem(position);
        positionCombo.setSelectedItem(position);
    }

    private void clearForm() {
        selectedPlayerId = null;
        if (teamCombo.getItemCount() > 0) {
            teamCombo.setSelectedIndex(0);
        }
        firstNameField.setText("");
        lastNameField.setText("");
        numberField.setText("");
        sportCombo.setSelectedIndex(0);
        updatePositionOptions();
        ageField.setText("");
        if (positionCombo.getItemCount() > 0) {
            positionCombo.setSelectedIndex(0);
        }
        weightField.setText("");
        heightField.setText("");
        scoreField.setText("0");
        basketballTable.clearSelection();
        volleyballTable.clearSelection();
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

    private Integer parseOptionalNonNegativeInt(JTextField field, String label) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return null;
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

    private BigDecimal parseOptionalDecimal(JTextField field, String label) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(text);
            if (value.signum() < 0) {
                throw new IllegalArgumentException(label + " must be zero or greater.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid decimal number.");
        }
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
        JLabel titleLabel = new JLabel("Player Details");
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

        addFormField(panel, 1, 0, "Team", teamCombo);
        addFormField(panel, 1, 1, "First Name", firstNameField);
        addFormField(panel, 2, 0, "Last Name", lastNameField);
        addFormField(panel, 2, 1, "Jersey Number", numberField);
        addFormField(panel, 3, 0, "Sport", sportCombo);
        addFormField(panel, 3, 1, "Position", positionCombo);
        addFormField(panel, 4, 0, "Age (optional)", ageField);
        addFormField(panel, 4, 1, "Individual Score", scoreField);
        addFormField(panel, 5, 0, "Weight (kg) (optional)", weightField);
        addFormField(panel, 5, 1, "Height (m) (optional)", heightField);

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
