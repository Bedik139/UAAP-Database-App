import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.SQLException;
import java.util.List;
public class EventManagerPanel extends JPanel {

    private final EventDAO eventDAO = new EventDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    // Form fields
    private JTextField idField;
    private JTextField nameField;
    private JComboBox<String> sportField;
    private JTextField dateField;   // yyyy-MM-dd
    private JTextField startField;  // HH:mm:ss
    private JTextField endField;    // HH:mm:ss
    private JTextField venueField;

    // Buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;

    public EventManagerPanel() {
        setLayout(new BorderLayout(10, 10));

        // Top form panel
        idField = new JTextField();
        idField.setEditable(false);
        idField.setToolTipText("Auto-filled when you select an event.");

        nameField = new JTextField();
        nameField.setToolTipText("Enter the name of the event.");
        sportField = new JComboBox<>(new String[]{"Basketball", "Volleyball"});
        sportField.setToolTipText("Pick the sport for this event.");
        dateField = new JTextField();
        dateField.setToolTipText("Format: YYYY-MM-DD (example: 2025-11-05)");
        startField = new JTextField();
        startField.setToolTipText("Format: HH:MM:SS (24-hour)");
        endField = new JTextField();
        endField.setToolTipText("Format: HH:MM:SS (24-hour)");
        venueField = new JTextField();
        venueField.setToolTipText("Enter the full venue or address.");

        JPanel formPanel = buildFormPanel();
        add(formPanel, BorderLayout.NORTH);

        // Center table panel
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Sport", "Date", "Start", "End", "Venue"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // table cells not directly editable
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Sync selected row -> form fields
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
                    nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                    sportField.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 2)));
                    dateField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
                    startField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
                    endField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
                    venueField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom button panel
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

        add(buttonPanel, BorderLayout.SOUTH);

        // Hook up button actions
        addButton.addActionListener(e -> handleAdd());
        updateButton.addActionListener(e -> handleUpdate());
        deleteButton.addActionListener(e -> handleDelete());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> reloadTable());

        // Initial table load
        reloadTable();
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        try {
            List<Event> events = eventDAO.getAllEvents();
            for (Event ev : events) {
                tableModel.addRow(new Object[]{
                        ev.getEventId(),
                        ev.getEventName(),
                        ev.getSport(),
                        ev.getMatchDate(),
                        ev.getEventTimeStart(),
                        ev.getEventTimeEnd(),
                        ev.getVenueAddress()
                });
            }
        } catch (SQLException ex) {
            showError("Error loading events:\n" + ex.getMessage());
        }
    }

    private void handleAdd() {
        try {
            Event ev = formToEvent(/* includeId = */ false);
            eventDAO.insertEvent(ev);
            showInfo("Event added.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Error adding event:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (idField.getText().isEmpty()) {
            showError("Select a row first.");
            return;
        }

        try {
            Event ev = formToEvent(/* includeId = */ true);
            eventDAO.updateEvent(ev);
            showInfo("Event updated.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Error updating event:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (idField.getText().isEmpty()) {
            showError("Select a row first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this event?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            int eventId = Integer.parseInt(idField.getText());
            eventDAO.deleteEvent(eventId);
            showInfo("Event deleted.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Error deleting event:\n" + ex.getMessage());
        }
    }

    private Event formToEvent(boolean includeId) {
        String name = nameField.getText().trim();
        String sport = (String) sportField.getSelectedItem();
        String dateStr = dateField.getText().trim();
        String startStr = startField.getText().trim();
        String endStr = endField.getText().trim();
        String venue = venueField.getText().trim();

        if (name.isEmpty() || sport.isEmpty() || dateStr.isEmpty()
                || startStr.isEmpty() || endStr.isEmpty() || venue.isEmpty()) {
            throw new IllegalArgumentException("All fields except ID are required.");
        }

        // Convert strings -> SQL Date/Time
        Date matchDate = Date.valueOf(dateStr);      // must be yyyy-MM-dd
        Time startTime = Time.valueOf(startStr);     // must be HH:mm:ss
        Time endTime   = Time.valueOf(endStr);

        Event ev = new Event(name, sport, matchDate, startTime, endTime, venue);

        if (includeId) {
            int eventId = Integer.parseInt(idField.getText().trim());
            ev.setEventId(eventId);
        }

        return ev;
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        sportField.setSelectedIndex(0);
        dateField.setText("");
        startField.setText("");
        endField.setText("");
        venueField.setText("");
        table.clearSelection();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Event Details"));

        addFormField(panel, 0, 0, "Event ID (auto)", idField);
        addFormField(panel, 0, 1, "Event Name", nameField);
        addFormField(panel, 1, 0, "Sport", sportField);
        addFormField(panel, 1, 1, "Match Date (YYYY-MM-DD)", dateField);
        addFormField(panel, 2, 0, "Start Time (HH:MM:SS)", startField);
        addFormField(panel, 2, 1, "End Time (HH:MM:SS)", endField);
        addWideFormField(panel, 3, "Venue Address", venueField);

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

    private void addWideFormField(JPanel panel, int row, String labelText, JComponent component) {
        GridBagConstraints labelGbc = baseGbc(row, 0);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(labelText), labelGbc);

        GridBagConstraints fieldGbc = baseGbc(row, 1);
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1.0;
        fieldGbc.gridwidth = 3;
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
