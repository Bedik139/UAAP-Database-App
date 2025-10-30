import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerManagerPanel extends JPanel {

    private final PlayerDAO playerDAO = new PlayerDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Player> cachedPlayers = new ArrayList<>();

    private JTextField idField;
    private JComboBox<Team> teamCombo;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField numberField;
    private JTextField ageField;
    private JTextField positionField;
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
        initTable();
        initButtons();
        reloadTeams();
        reloadTable();
    }

    private void initForm() {
        idField = new JTextField();
        idField.setEditable(false);
        idField.setToolTipText("Auto-filled when you select a player.");
        teamCombo = new JComboBox<>();
        teamCombo.setToolTipText("Pick the team this player belongs to.");
        firstNameField = new JTextField();
        firstNameField.setToolTipText("Enter the player's given name.");
        lastNameField = new JTextField();
        lastNameField.setToolTipText("Enter the player's surname.");
        numberField = new JTextField();
        numberField.setToolTipText("Jersey number. Must be unique per team.");
        ageField = new JTextField();
        ageField.setToolTipText("Optional. Enter age in years.");
        positionField = new JTextField();
        positionField.setToolTipText("Optional. e.g., Setter, Opposite Spiker.");
        weightField = new JTextField();
        weightField.setToolTipText("Optional. Weight in kilograms (decimal allowed).");
        heightField = new JTextField();
        heightField.setToolTipText("Optional. Height in centimeters (decimal allowed).");
        scoreField = new JTextField("0");
        scoreField.setToolTipText("Running total score for this player.");

        JPanel formPanel = buildFormPanel();
        add(formPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Team", "First Name", "Last Name", "Number",
                        "Age", "Position", "Weight", "Height", "Score"}, 0
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
                if (row >= 0 && row < cachedPlayers.size()) {
                    Player player = cachedPlayers.get(row);
                    populateForm(player);
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
        refreshButton.addActionListener(e -> {
            reloadTeams();
            reloadTable();
        });

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

    private void reloadTable() {
        tableModel.setRowCount(0);
        cachedPlayers.clear();
        try {
            cachedPlayers = playerDAO.getAllPlayers();
            for (Player player : cachedPlayers) {
                tableModel.addRow(new Object[]{
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
                });
            }
        } catch (SQLException ex) {
            showError("Error loading players:\n" + ex.getMessage());
        }
    }

    private void handleAdd() {
        try {
            Player player = formToPlayer(false);
            playerDAO.insertPlayer(player);
            showInfo("Player added.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Error adding player:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (idField.getText().trim().isEmpty()) {
            showError("Select a row first.");
            return;
        }

        try {
            Player player = formToPlayer(true);
            playerDAO.updatePlayer(player);
            showInfo("Player updated.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Error updating player:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (idField.getText().trim().isEmpty()) {
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
            int playerId = Integer.parseInt(idField.getText().trim());
            playerDAO.deletePlayer(playerId);
            showInfo("Player deleted.");
            reloadTable();
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
        Integer age = parseOptionalNonNegativeInt(ageField, "Age");
        String position = positionField.getText().trim();
        if (position.isEmpty()) {
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
                age,
                position,
                weight,
                height,
                score
        );
        player.setTeamName(selectedTeam.getTeamName());

        if (includeId) {
            player.setPlayerId(Integer.parseInt(idField.getText().trim()));
        }

        return player;
    }

    private void populateForm(Player player) {
        idField.setText(String.valueOf(player.getPlayerId()));
        selectTeamInCombo(player.getTeamId());
        firstNameField.setText(player.getFirstName());
        lastNameField.setText(player.getLastName());
        numberField.setText(String.valueOf(player.getPlayerNumber()));
        ageField.setText(player.getAge() != null ? String.valueOf(player.getAge()) : "");
        positionField.setText(player.getPosition() != null ? player.getPosition() : "");
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

    private void clearForm() {
        idField.setText("");
        if (teamCombo.getItemCount() > 0) {
            teamCombo.setSelectedIndex(0);
        }
        firstNameField.setText("");
        lastNameField.setText("");
        numberField.setText("");
        ageField.setText("");
        positionField.setText("");
        weightField.setText("");
        heightField.setText("");
        scoreField.setText("0");
        table.clearSelection();
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
        panel.setBorder(BorderFactory.createTitledBorder("Player Details"));

        addFormField(panel, 0, 0, "Player ID (auto)", idField);
        addFormField(panel, 0, 1, "Team", teamCombo);
        addFormField(panel, 1, 0, "First Name", firstNameField);
        addFormField(panel, 1, 1, "Last Name", lastNameField);
        addFormField(panel, 2, 0, "Jersey Number", numberField);
        addFormField(panel, 2, 1, "Age (optional)", ageField);
        addFormField(panel, 3, 0, "Position (optional)", positionField);
        addFormField(panel, 3, 1, "Individual Score", scoreField);
        addFormField(panel, 4, 0, "Weight kg (optional)", weightField);
        addFormField(panel, 4, 1, "Height cm (optional)", heightField);

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
