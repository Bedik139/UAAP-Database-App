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
import java.util.List;

public class EventManagerPanel extends JPanel {

    private static final String[] SPORT_OPTIONS = {"Basketball", "Volleyball"};
    private static final String[] STATUS_OPTIONS = {"Scheduled", "Active", "Completed", "Cancelled"};

    private final EventDAO eventDAO = new EventDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField idField;
    private JTextField nameField;
    private JComboBox<String> sportField;
    private JTextField dateField;
    private JTextField startField;
    private JTextField endField;
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

    private void initForm() {
        idField = new JTextField();
        idField.setEditable(false);
        idField.setToolTipText("Auto-filled when you select an event.");

        nameField = new JTextField();
        sportField = new JComboBox<>(SPORT_OPTIONS);
        dateField = new JTextField();
        dateField.setToolTipText("Format: yyyy-mm-dd");
        startField = new JTextField();
        startField.setToolTipText("Format: HH:MM:SS");
        endField = new JTextField();
        endField.setToolTipText("Format: HH:MM:SS");
        venueCombo = new JComboBox<>(Venue.values());
        capacityField = new JTextField();
        capacityField.setEditable(false);
        capacityField.setToolTipText("Auto-filled based on venue selection.");
        statusField = new JComboBox<>(STATUS_OPTIONS);
        venueCombo.addActionListener(e -> autoFillCapacity());

        add(buildFormPanel(), BorderLayout.NORTH);
        autoFillCapacity();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Event Details"));

        addFormField(panel, 0, 0, "Event ID (auto)", idField);
        addFormField(panel, 0, 1, "Event Name", nameField);
        addFormField(panel, 1, 0, "Sport", sportField);
        addFormField(panel, 1, 1, "Match Date", dateField);
        addFormField(panel, 2, 0, "Start Time", startField);
        addFormField(panel, 2, 1, "End Time", endField);
        addFormField(panel, 3, 0, "Venue", venueCombo);
        addFormField(panel, 3, 1, "Venue Capacity", capacityField);
        addFormField(panel, 4, 0, "Status", statusField);

        return panel;
    }

    private void addFormField(JPanel panel, int row, int col, String label, java.awt.Component component) {
        GridBagConstraints labelGbc = baseGbc(row, col * 2);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), labelGbc);

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

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear Form");
        refreshButton = new JButton("Refresh");

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
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        sportField.setSelectedItem(tableModel.getValueAt(row, 2));
        dateField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        startField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        endField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
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
        String name = nameField.getText().trim();
        String dateText = dateField.getText().trim();
        String startText = startField.getText().trim();
        String endText = endField.getText().trim();
        Venue venue = (Venue) venueCombo.getSelectedItem();
        String capacityText = capacityField.getText().trim();

        if (name.isEmpty() || dateText.isEmpty() || startText.isEmpty() || endText.isEmpty() || venue == null || capacityText.isEmpty()) {
            throw new IllegalArgumentException("All fields except ID are required.");
        }

        Date matchDate = Date.valueOf(dateText);
        Time start = Time.valueOf(startText);
        Time end = Time.valueOf(endText);
        int capacity = Integer.parseInt(capacityText);

        Event event = new Event(
                includeId ? Integer.parseInt(idField.getText().trim()) : 0,
                name,
                (String) sportField.getSelectedItem(),
                matchDate,
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
        nameField.setText("");
        sportField.setSelectedIndex(0);
        dateField.setText("");
        startField.setText("");
        endField.setText("");
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
                        event.getMatchDate(),
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
}
