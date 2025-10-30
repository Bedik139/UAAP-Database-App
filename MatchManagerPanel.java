import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Date;
import java.sql.Time;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatchManagerPanel extends JPanel {

    private final MatchDAO matchDAO = new MatchDAO();
    private final EventDAO eventDAO = new EventDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Match> cachedMatches = new ArrayList<>();

    private JTextField idField;
    private JComboBox<Event> eventCombo;
    private JComboBox<String> typeCombo;
    private JTextField dateField;
    private JTextField startField;
    private JTextField endField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;

    public MatchManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        initForm();
        initTable();
        initButtons();
        reloadEvents();
        reloadTable();
    }

    private void initForm() {
        idField = new JTextField();
        idField.setEditable(false);
        idField.setToolTipText("Auto-filled when selecting a match.");

        eventCombo = new JComboBox<>();
        eventCombo.setToolTipText("Event that hosts this match.");

        typeCombo = new JComboBox<>(new String[]{
                "Elimination_Round",
                "Semifinals",
                "Finals"
        });
        typeCombo.setToolTipText("Competition stage of the match.");

        dateField = new JTextField();
        dateField.setToolTipText("Format: YYYY-MM-DD");

        startField = new JTextField();
        startField.setToolTipText("Format: HH:MM:SS");

        endField = new JTextField();
        endField.setToolTipText("Format: HH:MM:SS");

        add(buildFormPanel(), BorderLayout.NORTH);
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Match ID", "Event", "Type", "Date", "Start", "End"}, 0
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
                if (row >= 0 && row < cachedMatches.size()) {
                    Match match = cachedMatches.get(row);
                    populateForm(match);
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
            reloadEvents();
            reloadTable();
        });
    }

    private void reloadEvents() {
        try {
            List<Event> events = eventDAO.getAllEvents();
            DefaultComboBoxModel<Event> model = new DefaultComboBoxModel<>();
            for (Event event : events) {
                model.addElement(event);
            }
            eventCombo.setModel(model);
            if (model.getSize() > 0) {
                eventCombo.setSelectedIndex(0);
            }
        } catch (SQLException ex) {
            showError("Error loading events:\n" + ex.getMessage());
        }
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        cachedMatches.clear();

        try {
            cachedMatches = matchDAO.getAllMatches();
            for (Match match : cachedMatches) {
                tableModel.addRow(new Object[]{
                        match.getMatchId(),
                        match.getEventName(),
                        match.getMatchType(),
                        match.getMatchDate(),
                        match.getMatchTimeStart(),
                        match.getMatchTimeEnd()
                });
            }
        } catch (SQLException ex) {
            showError("Error loading matches:\n" + ex.getMessage());
        }
    }

    private void handleAdd() {
        try {
            Match match = formToMatch(false);
            matchDAO.insertMatch(match);
            showInfo("Match saved.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to add match:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (idField.getText().trim().isEmpty()) {
            showError("Select a match first.");
            return;
        }

        try {
            Match match = formToMatch(true);
            matchDAO.updateMatch(match);
            showInfo("Match updated.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to update match:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (idField.getText().trim().isEmpty()) {
            showError("Select a match first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected match?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int matchId = Integer.parseInt(idField.getText().trim());
            matchDAO.deleteMatch(matchId);
            showInfo("Match deleted.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to delete match:\n" + ex.getMessage());
        }
    }

    private Match formToMatch(boolean includeId) {
        if (eventCombo.getSelectedItem() == null) {
            throw new IllegalArgumentException("Pick an event first.");
        }

        Event event = (Event) eventCombo.getSelectedItem();
        String type = (String) typeCombo.getSelectedItem();
        String dateText = dateField.getText().trim();
        String startText = startField.getText().trim();
        String endText = endField.getText().trim();

        if (dateText.isEmpty() || startText.isEmpty() || endText.isEmpty()) {
            throw new IllegalArgumentException("Date and time fields are required.");
        }

        Date date = Date.valueOf(dateText);
        Time start = Time.valueOf(startText);
        Time end = Time.valueOf(endText);

        Match match = new Match(event.getEventId(), type, date, start, end);
        if (includeId) {
            match.setMatchId(Integer.parseInt(idField.getText().trim()));
        }
        return match;
    }

    private void populateForm(Match match) {
        idField.setText(String.valueOf(match.getMatchId()));
        selectEvent(match.getEventId());
        typeCombo.setSelectedItem(match.getMatchType());
        dateField.setText(match.getMatchDate().toString());
        startField.setText(match.getMatchTimeStart().toString());
        endField.setText(match.getMatchTimeEnd().toString());
    }

    private void selectEvent(int eventId) {
        ComboBoxModel<Event> model = eventCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Event event = model.getElementAt(i);
            if (event.getEventId() == eventId) {
                eventCombo.setSelectedIndex(i);
                return;
            }
        }
        eventCombo.setSelectedIndex(-1);
    }

    private void clearForm() {
        idField.setText("");
        if (eventCombo.getItemCount() > 0) {
            eventCombo.setSelectedIndex(0);
        } else {
            eventCombo.setSelectedIndex(-1);
        }
        typeCombo.setSelectedIndex(0);
        dateField.setText("");
        startField.setText("");
        endField.setText("");
        table.clearSelection();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Match Details"));

        addFormField(panel, 0, 0, "Match ID (auto)", idField);
        addFormField(panel, 0, 1, "Event", eventCombo);
        addFormField(panel, 1, 0, "Match Type", typeCombo);
        addFormField(panel, 1, 1, "Match Date", dateField);
        addFormField(panel, 2, 0, "Start Time", startField);
        addFormField(panel, 2, 1, "End Time", endField);

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
