import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
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

public class MatchManagerPanel extends JPanel {

    private static final String[] MATCH_TYPES = {"Elimination_Round", "Semifinals", "Finals"};
    private static final String[] STATUS_OPTIONS = {"Scheduled", "Completed", "Cancelled", "Postponed"};

    private final MatchDAO matchDAO = new MatchDAO();
    private final EventDAO eventDAO = new EventDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private final MatchSpotlightPanel spotlightPanel = new MatchSpotlightPanel();

    private JTextField idField;
    private JComboBox<Event> eventCombo;
    private JComboBox<String> typeCombo;
    private JTextField dateField;
    private JTextField startField;
    private JTextField endField;
    private JComboBox<String> statusCombo;
    private JTextField summaryField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;

    public MatchManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        initForm();
        initTable();
        JPanel centerDeck = new JPanel(new BorderLayout(12, 0));
        centerDeck.setOpaque(false);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        UAAPTheme.elevate(tableScroll);
        centerDeck.add(tableScroll, BorderLayout.CENTER);
        centerDeck.add(spotlightPanel, BorderLayout.EAST);
        add(centerDeck, BorderLayout.CENTER);
        spotlightPanel.showcaseMatch(-1);
        initButtons();
        reloadEvents();
        reloadTable();
    }

    private void initForm() {
        idField = new JTextField();
        idField.setEditable(false);

        eventCombo = new JComboBox<>();
        typeCombo = new JComboBox<>(MATCH_TYPES);
        dateField = new JTextField();
        startField = new JTextField();
        endField = new JTextField();
        statusCombo = new JComboBox<>(STATUS_OPTIONS);
        statusCombo.setSelectedItem("Scheduled");
        summaryField = new JTextField();
        summaryField.setEditable(false);
        summaryField.setFocusable(false);
        summaryField.setToolTipText("Auto-generated from match team totals.");

        add(buildForm(), BorderLayout.NORTH);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Match Details"));

        addFormField(panel, 0, 0, "Match ID (auto)", idField);
        addFormField(panel, 0, 1, "Event", eventCombo);
        addFormField(panel, 1, 0, "Match Type", typeCombo);
        addFormField(panel, 1, 1, "Status", statusCombo);
        addFormField(panel, 2, 0, "Match Date", dateField);
        addFormField(panel, 2, 1, "Start Time", startField);
        addFormField(panel, 3, 0, "End Time", endField);
        addFormField(panel, 3, 1, "Score Summary", summaryField);

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
        return gbc;
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Match ID", "Event", "Type", "Date", "Start", "End", "Status", "Score Summary"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        populateFormFromTable(row);
                        showcaseRow(row);
                    } else {
                        spotlightPanel.showcaseMatch(-1);
                    }
                }
            }
        });

        UAAPTheme.styleTable(table);
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
            reloadEvents();
            reloadTable();
        });

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        panel.add(refreshButton);

        add(panel, BorderLayout.SOUTH);
    }

    private void populateFormFromTable(int row) {
        idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        selectEventByLabel(String.valueOf(tableModel.getValueAt(row, 1)));
        typeCombo.setSelectedItem(tableModel.getValueAt(row, 2));
        dateField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        startField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        endField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        statusCombo.setSelectedItem(tableModel.getValueAt(row, 6));
        Object summary = tableModel.getValueAt(row, 7);
        summaryField.setText(summary != null ? summary.toString() : "");
    }

    private void showcaseRow(int row) {
        Object idValue = tableModel.getValueAt(row, 0);
        try {
            int matchId = Integer.parseInt(String.valueOf(idValue));
            spotlightPanel.showcaseMatch(matchId);
        } catch (NumberFormatException ex) {
            spotlightPanel.showcaseMatch(-1);
        }
    }

    private void selectEventByLabel(String label) {
        ComboBoxModel<Event> model = eventCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Event event = model.getElementAt(i);
            if (label.equals(event.toString())) {
                eventCombo.setSelectedIndex(i);
                return;
            }
        }
        eventCombo.setSelectedIndex(-1);
    }

    private void handleAdd() {
        try {
            Match match = formToMatch(false);
            matchDAO.insertMatch(match);
            showInfo("Match added.");
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

        Match match = new Match(
                includeId ? Integer.parseInt(idField.getText().trim()) : 0,
                event.getEventId(),
                event.getEventName(),
                type,
                date,
                start,
                end,
                (String) statusCombo.getSelectedItem(),
                summaryField.getText().trim().isEmpty() ? null : summaryField.getText().trim()
        );
        return match;
    }

    private void clearForm() {
        idField.setText("");
        if (eventCombo.getItemCount() > 0) {
            eventCombo.setSelectedIndex(0);
        }
        typeCombo.setSelectedIndex(0);
        dateField.setText("");
        startField.setText("");
        endField.setText("");
        statusCombo.setSelectedItem("Scheduled");
        summaryField.setText("");
        table.clearSelection();
        spotlightPanel.showcaseMatch(-1);
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
            showError("Unable to load events:\n" + ex.getMessage());
        }
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        try {
            List<Match> matches = matchDAO.getAllMatches();
            for (Match match : matches) {
                tableModel.addRow(new Object[]{
                        match.getMatchId(),
                        match.toString(),
                        match.getMatchType(),
                        match.getMatchDate(),
                        match.getMatchTimeStart(),
                        match.getMatchTimeEnd(),
                        match.getStatus(),
                        match.getScoreSummary()
                });
            }
        } catch (SQLException ex) {
            showError("Unable to load matches:\n" + ex.getMessage());
        }
        spotlightPanel.showcaseMatch(-1);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
