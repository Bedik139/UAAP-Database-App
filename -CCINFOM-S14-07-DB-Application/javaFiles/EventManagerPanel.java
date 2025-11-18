import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

public class EventManagerPanel extends JPanel {

    private static final String[] SPORT_OPTIONS = {"Basketball", "Volleyball"};
    private static final String[] EVENT_NAME_OPTIONS = {
        "Season Opener", "Mid-Season Game", "Championship Finals",
        "Semifinals Round 1", "Semifinals Round 2", "Quarterfinals",
        "Elimination Round", "All-Star Game", "Special Match"
    };
    private static final String[] STATUS_OPTIONS = {"Scheduled", "Active", "Completed", "Cancelled"};

    private final EventDAO eventDAO = new EventDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField idField;
    private JTextField seasonField;
    private JComboBox<String> nameField;
    private JComboBox<String> sportField;
    private JTextField dateField;
    private JButton datePickerButton;
    private JComboBox<String> startField;
    private JComboBox<String> endField;
    private JComboBox<Venue> venueCombo;
    private JTextField capacityField;
    private JComboBox<String> statusField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;
    private boolean updatingForm;

    public EventManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        initForm();
        initTable();
        initButtons();
        reloadTable();
    }

    private String[] generateMilitaryTimeOptions() {
        String[] times = new String[96]; // 24 hours * 4 (15-minute intervals)
        int index = 0;
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                times[index++] = String.format("%02d:%02d:00", hour, minute);
            }
        }
        return times;
    }

    private void initForm() {
        idField = new JTextField();
        idField.setEditable(false);
        idField.setToolTipText("Auto-filled when you select an event.");
        UAAPTheme.styleTextField(idField);

        seasonField = new JTextField("");
        seasonField.setToolTipText("Season number (e.g., 87 for S87)");
        UAAPTheme.styleTextField(seasonField);

        nameField = new JComboBox<>(EVENT_NAME_OPTIONS);
        nameField.setEditable(true);
        UAAPTheme.styleComboBox(nameField);
        
        sportField = new JComboBox<>(SPORT_OPTIONS);
        UAAPTheme.styleComboBox(sportField);
        
        dateField = new JTextField();
        dateField.setToolTipText("Format: yyyy-mm-dd");
        dateField.setEditable(false); // Make read-only, use button to select
        UAAPTheme.styleTextField(dateField);
        
        // Add calendar button next to date field
        datePickerButton = new JButton("...");
        datePickerButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        datePickerButton.setToolTipText("Select date from calendar");
        UAAPTheme.styleInfoButton(datePickerButton);
        datePickerButton.addActionListener(e -> showDatePicker());
        
        startField = new JComboBox<>(generateMilitaryTimeOptions());
        startField.setEditable(true);
        startField.setToolTipText("Select or enter start time in military format");
        UAAPTheme.styleComboBox(startField);
        
        endField = new JComboBox<>(generateMilitaryTimeOptions());
        endField.setEditable(true);
        endField.setToolTipText("Select or enter end time in military format");
        UAAPTheme.styleComboBox(endField);
        
        venueCombo = new JComboBox<>(Venue.values());
        UAAPTheme.styleComboBox(venueCombo);
        venueCombo.addActionListener(e -> autoFillCapacity());
        
        capacityField = new JTextField();
        capacityField.setEditable(false);
        capacityField.setToolTipText("Auto-filled based on venue selection.");
        UAAPTheme.styleTextField(capacityField);
        
        statusField = new JComboBox<>(STATUS_OPTIONS);
        UAAPTheme.styleComboBox(statusField);

        add(buildFormPanel(), BorderLayout.NORTH);
        autoFillCapacity();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UAAPTheme.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UAAPTheme.CARD_BORDER, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Title
        JLabel titleLabel = new JLabel("Event Details");
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

        // Hidden fields for ID and capacity - kept for functionality but not displayed
        idField.setVisible(false);
        capacityField.setVisible(false);

        addFormField(panel, 1, 0, "Season", seasonField);
        addFormField(panel, 1, 1, "Event Type", nameField);
        addFormField(panel, 2, 0, "Sport", sportField);
        addFormFieldWithButton(panel, 2, 1, "Event Date", dateField, datePickerButton);
        addFormField(panel, 3, 0, "Start Time", startField);
        addFormField(panel, 3, 1, "End Time", endField);
        addFormField(panel, 4, 0, "Venue", venueCombo);
        addFormField(panel, 4, 1, "Status", statusField);

        return panel;
    }

    private void addFormField(JPanel panel, int row, int col, String label, java.awt.Component component) {
        JLabel jLabel = new JLabel(label + ":");
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
    
    private void addFormFieldWithButton(JPanel panel, int row, int col, String label, java.awt.Component component, JButton button) {
        JLabel jLabel = new JLabel(label + ":");
        jLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        jLabel.setForeground(UAAPTheme.TEXT_PRIMARY);
        
        GridBagConstraints labelGbc = baseGbc(row, col * 2);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(jLabel, labelGbc);

        // Create a panel to hold both the text field and button
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.setBackground(UAAPTheme.CARD_BACKGROUND);
        fieldPanel.add(component, BorderLayout.CENTER);
        fieldPanel.add(button, BorderLayout.EAST);
        
        GridBagConstraints fieldGbc = baseGbc(row, col * 2 + 1);
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 0.5;
        panel.add(fieldPanel, fieldGbc);
    }
    
    private void showDatePicker() {
        // Parse current date from field, or use today as default
        LocalDate initialDate = LocalDate.now();
        String currentText = dateField.getText().trim();
        if (!currentText.isEmpty()) {
            try {
                initialDate = LocalDate.parse(currentText);
            } catch (Exception ex) {
                // Use today if parsing fails
            }
        }
        
        // Show date picker dialog
        LocalDate selectedDate = DatePickerPanel.showDatePickerDialog(this, initialDate);
        
        // Update field if a date was selected
        if (selectedDate != null) {
            dateField.setText(selectedDate.toString());
        }
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Sport", "Date", "Start", "End", "Venue", "Capacity", "Status"}, 0
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
                        populateFormFromTable(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        UAAPTheme.elevate(scrollPane);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        buttonPanel.setBackground(UAAPTheme.LIGHT_SURFACE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UAAPTheme.CARD_BORDER));

        addButton = new JButton("Add Event");
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

    private void populateFormFromTable(int row) {
        updatingForm = true;
        idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        
        // Parse the event name back into season and event type
        String fullEventName = String.valueOf(tableModel.getValueAt(row, 1));
        parseEventName(fullEventName);
        
        sportField.setSelectedItem(tableModel.getValueAt(row, 2));
        dateField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        startField.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 4)));
        endField.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 5)));
        Venue venue = Venue.fromName(String.valueOf(tableModel.getValueAt(row, 6)));
        venueCombo.setSelectedItem(venue);
        capacityField.setText(String.valueOf(tableModel.getValueAt(row, 7)));
        statusField.setSelectedItem(tableModel.getValueAt(row, 8));
        updatingForm = false;
    }

    private void handleAdd() {
        try {
            Event event = formToEvent(false);
            eventDAO.insertEvent(event);
            showInfo("Event added.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to add event:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (idField.getText().trim().isEmpty()) {
            showError("Select an event first.");
            return;
        }

        try {
            Event event = formToEvent(true);
            eventDAO.updateEvent(event);
            showInfo("Event updated.");
            reloadTable();
        } catch (Exception ex) {
            showError("Unable to update event:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (idField.getText().trim().isEmpty()) {
            showError("Select an event first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected event?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int eventId = Integer.parseInt(idField.getText().trim());
            eventDAO.deleteEvent(eventId);
            showInfo("Event deleted.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to delete event:\n" + ex.getMessage());
        }
    }

    private Event formToEvent(boolean includeId) {
        String seasonText = seasonField.getText().trim();
        String eventType = String.valueOf(nameField.getEditor().getItem()).trim();
        String sport = (String) sportField.getSelectedItem();
        String dateText = dateField.getText().trim();
        String startText = String.valueOf(startField.getEditor().getItem()).trim();
        String endText = String.valueOf(endField.getEditor().getItem()).trim();
        Venue venue = (Venue) venueCombo.getSelectedItem();
        String capacityText = capacityField.getText().trim();

        if (seasonText.isEmpty() || eventType.isEmpty() || dateText.isEmpty() || startText.isEmpty() || endText.isEmpty() || venue == null || capacityText.isEmpty()) {
            throw new IllegalArgumentException("All fields except ID are required.");
        }

        // Generate full event name: "UAAP S[Season] [Sport] - [Event Type]"
        String fullEventName = String.format("UAAP S%s %s - %s", seasonText, sport, eventType);

        Date eventDate = Date.valueOf(dateText);
        Time start = Time.valueOf(startText);
        Time end = Time.valueOf(endText);
        int capacity = Integer.parseInt(capacityText);

        Event event = new Event(
                includeId ? Integer.parseInt(idField.getText().trim()) : 0,
                fullEventName,
                sport,
                eventDate,
                start,
                end,
                venue.getDisplayName(),
                (String) statusField.getSelectedItem(),
                capacity
        );
        return event;
    }

    private void clearForm() {
        idField.setText("");
        seasonField.setText("87");
        nameField.setSelectedIndex(0);
        sportField.setSelectedIndex(0);
        dateField.setText("");
        startField.setSelectedIndex(0);
        endField.setSelectedIndex(0);
        updatingForm = true;
        venueCombo.setSelectedIndex(0);
        capacityField.setText("");
        updatingForm = false;
        autoFillCapacity();
        statusField.setSelectedIndex(0);
        table.clearSelection();
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        try {
            List<Event> events = eventDAO.getAllEvents();
            for (Event event : events) {
                tableModel.addRow(new Object[]{
                        event.getEventId(),
                        event.getEventName(),
                        event.getSport(),
                        event.getEventDate(),
                        event.getEventTimeStart(),
                        event.getEventTimeEnd(),
                        event.getVenueAddress(),
                        event.getVenueCapacity(),
                        event.getEventStatus()
                });
            }
        } catch (SQLException ex) {
            showError("Unable to load events:\n" + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void autoFillCapacity() {
        if (updatingForm) {
            return;
        }
        Venue venue = (Venue) venueCombo.getSelectedItem();
        capacityField.setText(venue != null ? String.valueOf(venue.getCapacity()) : "");
    }

    private void parseEventName(String fullEventName) {
        // Parse "UAAP S87 Basketball - Finals" format
        // Extract season, sport, and event type
        try {
            if (fullEventName.startsWith("UAAP S")) {
                String afterUAAP = fullEventName.substring(6); // Remove "UAAP S"
                
                // Find the space after season number
                int spaceIdx = afterUAAP.indexOf(' ');
                if (spaceIdx > 0) {
                    String season = afterUAAP.substring(0, spaceIdx);
                    seasonField.setText(season);
                    
                    // Find the " - " separator
                    int dashIdx = afterUAAP.indexOf(" - ");
                    if (dashIdx > 0) {
                        String eventType = afterUAAP.substring(dashIdx + 3);
                        nameField.getEditor().setItem(eventType);
                    }
                }
            } else {
                // Fallback: just set the full name as event type
                nameField.getEditor().setItem(fullEventName);
            }
        } catch (Exception e) {
            // If parsing fails, just use the full name
            nameField.getEditor().setItem(fullEventName);
        }
    }
}
