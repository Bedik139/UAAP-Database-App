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

public class EventPersonnelManagerPanel extends JPanel {

    private static final String[] ROLES = {
            "Usher",
            "Ticketing Agent",
            "Security",
            "Referee",
            "Scorekeeper",
            "Stat Crew",
            "Band Member",
            "Cheerleader",
            "Dance Troupe",
            "Singer",
            "Host",
            "Halftime Entertainment"
    };

    private final EventPersonnelDAO personnelDAO = new EventPersonnelDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final MatchDAO matchDAO = new MatchDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<EventPersonnel> cachedPersonnel = new ArrayList<>();

    private JTextField idField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField availabilityField;
    private JComboBox<String> roleCombo;
    private JTextField affiliationField;
    private JTextField contactField;
    private JComboBox<Event> eventCombo;
    private JComboBox<Match> matchCombo;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;

    private List<Match> allMatches = new ArrayList<>();

    public EventPersonnelManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        initForm();
        initTable();
        initButtons();
        reloadLookups();
        reloadTable();
    }

    private void initForm() {
        idField = new JTextField();
        idField.setEditable(false);
        idField.setToolTipText("Auto-filled when you select a personnel entry.");

        firstNameField = new JTextField();
        firstNameField.setToolTipText("First name of the personnel.");

        lastNameField = new JTextField();
        lastNameField.setToolTipText("Last name of the personnel.");

        availabilityField = new JTextField();
        availabilityField.setToolTipText("Availability description, e.g. 'Available', 'On Leave'.");

        roleCombo = new JComboBox<>(ROLES);
        roleCombo.setToolTipText("Assigned role during the event.");

        affiliationField = new JTextField();
        affiliationField.setToolTipText("Affiliated organization or team.");

        contactField = new JTextField();
        contactField.setToolTipText("Contact number, optional.");

        eventCombo = new JComboBox<>();
        eventCombo.setToolTipText("Event assignment.");
        eventCombo.addActionListener(e -> refreshMatchOptions());

        matchCombo = new JComboBox<>();
        matchCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list,
                                                                   Object value,
                                                                   int index,
                                                                   boolean isSelected,
                                                                   boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "No linked match" : value.toString());
                return this;
            }
        });
        matchCombo.setToolTipText("Optional specific match for the personnel.");

        add(buildFormPanel(), BorderLayout.NORTH);
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Role", "Availability", "Event", "Match", "Affiliation", "Contact"}, 0
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
                if (row >= 0 && row < cachedPersonnel.size()) {
                    EventPersonnel personnel = cachedPersonnel.get(row);
                    populateForm(personnel);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear Form");
        refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> handleAdd());
        updateButton.addActionListener(e -> handleUpdate());
        deleteButton.addActionListener(e -> handleDelete());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> {
            reloadLookups();
            reloadTable();
        });

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        panel.add(refreshButton);

        add(panel, BorderLayout.SOUTH);
    }

    private void reloadLookups() {
        reloadEvents();
        reloadMatches();
        refreshMatchOptions();
    }

    private void reloadEvents() {
        try {
            DefaultComboBoxModel<Event> model = new DefaultComboBoxModel<>();
            for (Event event : eventDAO.getAllEvents()) {
                model.addElement(event);
            }
            eventCombo.setModel(model);
        } catch (SQLException ex) {
            showError("Error loading events:\n" + ex.getMessage());
        }
    }

    private void reloadMatches() {
        try {
            allMatches = matchDAO.getAllMatches();
        } catch (SQLException ex) {
            showError("Error loading matches:\n" + ex.getMessage());
        }
    }

    private void refreshMatchOptions() {
        DefaultComboBoxModel<Match> model = new DefaultComboBoxModel<>();
        model.addElement(null);

        Event selectedEvent = (Event) eventCombo.getSelectedItem();
        if (selectedEvent != null) {
            for (Match match : allMatches) {
                if (match.getEventId() == selectedEvent.getEventId()) {
                    model.addElement(match);
                }
            }
        } else {
            for (Match match : allMatches) {
                model.addElement(match);
            }
        }
        matchCombo.setModel(model);
        matchCombo.setSelectedIndex(0);
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        cachedPersonnel.clear();

        try {
            cachedPersonnel = personnelDAO.getAllPersonnel();
            for (EventPersonnel personnel : cachedPersonnel) {
                tableModel.addRow(new Object[]{
                        personnel.getPersonnelId(),
                        personnel.getFirstName() + " " + personnel.getLastName(),
                        personnel.getRole(),
                        personnel.getAvailabilityStatus(),
                        personnel.getEventName(),
                        personnel.getMatchLabel() != null ? personnel.getMatchLabel() : "None",
                        personnel.getAffiliation(),
                        personnel.getContactNo() != null ? personnel.getContactNo() : ""
                });
            }
        } catch (SQLException ex) {
            showError("Error loading personnel:\n" + ex.getMessage());
        }
    }

    private void handleAdd() {
        try {
            EventPersonnel personnel = formToPersonnel(false);
            personnelDAO.insertPersonnel(personnel);
            showInfo("Personnel saved.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to add personnel:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (idField.getText().trim().isEmpty()) {
            showError("Select a personnel entry first.");
            return;
        }

        try {
            EventPersonnel personnel = formToPersonnel(true);
            personnelDAO.updatePersonnel(personnel);
            showInfo("Personnel updated.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to update personnel:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (idField.getText().trim().isEmpty()) {
            showError("Select a personnel entry first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected personnel?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int personnelId = Integer.parseInt(idField.getText().trim());
            personnelDAO.deletePersonnel(personnelId);
            showInfo("Personnel deleted.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to delete personnel:\n" + ex.getMessage());
        }
    }

    private EventPersonnel formToPersonnel(boolean includeId) {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String availability = availabilityField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        String affiliation = affiliationField.getText().trim();
        String contact = contactField.getText().trim();
        Event event = (Event) eventCombo.getSelectedItem();
        Match match = (Match) matchCombo.getSelectedItem();

        if (firstName.isEmpty() || lastName.isEmpty() || availability.isEmpty() || affiliation.isEmpty()) {
            throw new IllegalArgumentException("First name, last name, availability, and affiliation are required.");
        }
        if (event == null) {
            throw new IllegalArgumentException("Select an event.");
        }

        EventPersonnel personnel = new EventPersonnel(
                firstName,
                lastName,
                availability,
                role,
                affiliation,
                contact.isEmpty() ? null : contact,
                event.getEventId(),
                match != null ? match.getMatchId() : null
        );

        if (includeId) {
            personnel.setPersonnelId(Integer.parseInt(idField.getText().trim()));
        }

        return personnel;
    }

    private void populateForm(EventPersonnel personnel) {
        idField.setText(String.valueOf(personnel.getPersonnelId()));
        firstNameField.setText(personnel.getFirstName());
        lastNameField.setText(personnel.getLastName());
        availabilityField.setText(personnel.getAvailabilityStatus());
        roleCombo.setSelectedItem(personnel.getRole());
        affiliationField.setText(personnel.getAffiliation());
        contactField.setText(personnel.getContactNo() != null ? personnel.getContactNo() : "");
        selectEvent(personnel.getEventId());
        selectMatch(personnel.getMatchId());
    }

    private void selectEvent(int eventId) {
        selectComboItem(eventCombo, event -> event.getEventId() == eventId);
        refreshMatchOptions();
    }

    private void selectMatch(Integer matchId) {
        if (matchId == null) {
            matchCombo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < matchCombo.getItemCount(); i++) {
            Match item = matchCombo.getItemAt(i);
            if (item != null && item.getMatchId() == matchId) {
                matchCombo.setSelectedIndex(i);
                return;
            }
        }
        matchCombo.setSelectedIndex(0);
    }

    private <T> void selectComboItem(JComboBox<T> combo, java.util.function.Predicate<T> predicate) {
        ComboBoxModel<T> model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            T item = model.getElementAt(i);
            if (predicate.test(item)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(-1);
    }

    private void clearForm() {
        idField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        availabilityField.setText("");
        roleCombo.setSelectedIndex(0);
        affiliationField.setText("");
        contactField.setText("");
        if (eventCombo.getItemCount() > 0) {
            eventCombo.setSelectedIndex(0);
        }
        matchCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Event Personnel Details"));

        addFormField(panel, 0, 0, "Personnel ID (auto)", idField);
        addFormField(panel, 0, 1, "First Name", firstNameField);
        addFormField(panel, 1, 0, "Last Name", lastNameField);
        addFormField(panel, 1, 1, "Availability", availabilityField);
        addFormField(panel, 2, 0, "Role", roleCombo);
        addFormField(panel, 2, 1, "Affiliation", affiliationField);
        addFormField(panel, 3, 0, "Contact No.", contactField);
        addFormField(panel, 3, 1, "Event", eventCombo);
        addFormField(panel, 4, 0, "Match (optional)", matchCombo);

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

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
