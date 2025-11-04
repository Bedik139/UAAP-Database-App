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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeatAndTicketManagerPanel extends JPanel {

    private final SeatAndTicketDAO seatAndTicketDAO = new SeatAndTicketDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final MatchDAO matchDAO = new MatchDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<SeatAndTicket> cachedRecords = new ArrayList<>();

    private JTextField recordIdField;
    private JComboBox<Seat> seatCombo;
    private JComboBox<Event> eventCombo;
    private JComboBox<Customer> customerCombo;
    private JTextField saleDatetimeField;
    private JTextField priceField;
    private JComboBox<Ticket> ticketCombo;
    private JComboBox<Match> matchCombo;
    private JComboBox<String> statusCombo;
    private JTextField refundDatetimeField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;

    private List<Match> allMatches = new ArrayList<>();

    public SeatAndTicketManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        initForm();
        initTable();
        initButtons();
        reloadLookups();
        reloadTable();
    }

    private void initForm() {
        recordIdField = new JTextField();
        recordIdField.setEditable(false);
        recordIdField.setToolTipText("Auto-filled when you select a record.");

        seatCombo = new JComboBox<>();
        seatCombo.setToolTipText("Seat being sold.");

        eventCombo = new JComboBox<>();
        eventCombo.setToolTipText("Event associated with the ticket.");
        eventCombo.addActionListener(e -> refreshMatchOptions());

        customerCombo = new JComboBox<>();
        customerCombo.setToolTipText("Purchasing customer.");

        saleDatetimeField = new JTextField();
        saleDatetimeField.setToolTipText("Sale timestamp (YYYY-MM-DD HH:MM:SS). Leave blank to use current time.");

        priceField = new JTextField();
        priceField.setToolTipText("Final price paid by the customer.");

        ticketCombo = new JComboBox<>();
        ticketCombo.setToolTipText("Ticket template used for the sale.");

        matchCombo = new JComboBox<>();
        matchCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list,
                                                                   Object value,
                                                                   int index,
                                                                   boolean isSelected,
                                                                   boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("No linked match");
                } else {
                    setText(value.toString());
                }
                return this;
            }
        });
        matchCombo.setToolTipText("Optional specific match. Leave as 'No linked match' to set NULL.");

        statusCombo = new JComboBox<>(new String[]{"Sold", "Refunded"});
        statusCombo.addActionListener(e -> toggleRefundField());

        refundDatetimeField = new JTextField();
        refundDatetimeField.setToolTipText("Refund timestamp (YYYY-MM-DD HH:MM:SS). Leave blank to auto-fill when refunding.");
        refundDatetimeField.setEnabled(false);
        toggleRefundField();

        add(buildFormPanel(), BorderLayout.NORTH);
    }

    private void toggleRefundField() {
        boolean refunded = "Refunded".equalsIgnoreCase((String) statusCombo.getSelectedItem());
        refundDatetimeField.setEnabled(refunded);
        if (!refunded) {
            refundDatetimeField.setText("");
        }
    }

    private void initTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"Record ID", "Seat", "Event", "Customer", "Sold At", "Price", "Ticket", "Match", "Status", "Refunded At"}, 0
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
                if (row >= 0 && row < cachedRecords.size()) {
                    SeatAndTicket record = cachedRecords.get(row);
                    populateForm(record);
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
            reloadLookups();
            reloadTable();
        });
    }

    private void reloadLookups() {
        reloadSeats();
        reloadEvents();
        reloadCustomers();
        reloadTickets();
        reloadMatches();
        refreshMatchOptions();
    }

    private void reloadSeats() {
        try {
            DefaultComboBoxModel<Seat> model = new DefaultComboBoxModel<>();
            for (Seat seat : seatDAO.getAllSeats()) {
                model.addElement(seat);
            }
            seatCombo.setModel(model);
        } catch (SQLException ex) {
            showError("Error loading seats:\n" + ex.getMessage());
        }
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

    private void reloadCustomers() {
        try {
            DefaultComboBoxModel<Customer> model = new DefaultComboBoxModel<>();
            for (Customer customer : customerDAO.getAllCustomers()) {
                model.addElement(customer);
            }
            customerCombo.setModel(model);
        } catch (SQLException ex) {
            showError("Error loading customers:\n" + ex.getMessage());
        }
    }

    private void reloadTickets() {
        try {
            DefaultComboBoxModel<Ticket> model = new DefaultComboBoxModel<>();
            for (Ticket ticket : ticketDAO.getAllTickets()) {
                model.addElement(ticket);
            }
            ticketCombo.setModel(model);
        } catch (SQLException ex) {
            showError("Error loading tickets:\n" + ex.getMessage());
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
        cachedRecords.clear();

        try {
            cachedRecords = seatAndTicketDAO.getAllRecords();
            for (SeatAndTicket record : cachedRecords) {
                tableModel.addRow(new Object[]{
                        record.getRecordId(),
                        record.getSeatLabel(),
                        record.getEventName(),
                        record.getCustomerName(),
                        record.getSaleDatetime(),
                        record.getPriceSold(),
                        record.getTicketLabel(),
                        record.getMatchLabel() != null ? record.getMatchLabel() : "None",
                        record.getSaleStatus(),
                        record.getRefundDatetime() != null ? record.getRefundDatetime() : ""
                });
            }
        } catch (SQLException ex) {
            showError("Error loading seat and ticket records:\n" + ex.getMessage());
        }
    }

    private void handleAdd() {
        try {
            SeatAndTicket record = formToRecord(false);
            seatAndTicketDAO.insertRecord(record);
            showInfo("Seat and ticket record saved.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to add record:\n" + ex.getMessage());
        }
    }

    private void handleUpdate() {
        if (recordIdField.getText().trim().isEmpty()) {
            showError("Select a record first.");
            return;
        }

        try {
            SeatAndTicket record = formToRecord(true);
            seatAndTicketDAO.updateRecord(record);
            showInfo("Seat and ticket record updated.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to update record:\n" + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (recordIdField.getText().trim().isEmpty()) {
            showError("Select a record first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected record?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int recordId = Integer.parseInt(recordIdField.getText().trim());
            seatAndTicketDAO.deleteRecord(recordId);
            showInfo("Record deleted.");
            reloadTable();
            clearForm();
        } catch (Exception ex) {
            showError("Unable to delete record:\n" + ex.getMessage());
        }
    }

    private SeatAndTicket formToRecord(boolean includeId) {
        if (seatCombo.getSelectedItem() == null ||
                eventCombo.getSelectedItem() == null ||
                customerCombo.getSelectedItem() == null ||
                ticketCombo.getSelectedItem() == null) {
            throw new IllegalArgumentException("Seat, event, customer, and ticket must be selected.");
        }

        Seat seat = (Seat) seatCombo.getSelectedItem();
        Event event = (Event) eventCombo.getSelectedItem();
        Customer customer = (Customer) customerCombo.getSelectedItem();
        Ticket ticket = (Ticket) ticketCombo.getSelectedItem();
        Match match = (Match) matchCombo.getSelectedItem();

        Timestamp saleTime = parseTimestamp(saleDatetimeField);
        BigDecimal price = parsePrice(priceField);
        Integer matchId = (match != null) ? match.getMatchId() : null;
        String saleStatus = (String) statusCombo.getSelectedItem();
        Timestamp refundTime = parseRefundTimestamp(refundDatetimeField);
        if ("Refunded".equalsIgnoreCase(saleStatus) && refundTime == null) {
            refundTime = Timestamp.valueOf(LocalDateTime.now());
        }
        if (!"Refunded".equalsIgnoreCase(saleStatus)) {
            refundTime = null;
        }

        SeatAndTicket record = new SeatAndTicket(
                seat.getSeatId(),
                event.getEventId(),
                customer.getCustomerId(),
                saleTime,
                price,
                ticket.getTicketId(),
                matchId,
                saleStatus
        );
        record.setRefundDatetime(refundTime);

        if (includeId) {
            record.setRecordId(Integer.parseInt(recordIdField.getText().trim()));
        }

        return record;
    }

    private Timestamp parseTimestamp(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
        return Timestamp.valueOf(text.replace('T', ' '));
    }

    private Timestamp parseRefundTimestamp(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        return Timestamp.valueOf(text.replace('T', ' '));
    }

    private BigDecimal parsePrice(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Price is required.");
        }
        try {
            BigDecimal value = new BigDecimal(text);
            if (value.signum() < 0) {
                throw new IllegalArgumentException("Price must be zero or greater.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Price must be a valid decimal number.");
        }
    }

    private void populateForm(SeatAndTicket record) {
        recordIdField.setText(String.valueOf(record.getRecordId()));
        selectSeat(record.getSeatId());
        selectEvent(record.getEventId());
        selectCustomer(record.getCustomerId());
        saleDatetimeField.setText(record.getSaleDatetime().toString());
        priceField.setText(record.getPriceSold().toPlainString());
        selectTicket(record.getTicketId());
        selectMatch(record.getMatchId());
        statusCombo.setSelectedItem(record.getSaleStatus());
        if (record.getRefundDatetime() != null) {
            refundDatetimeField.setText(record.getRefundDatetime().toString());
        } else {
            refundDatetimeField.setText("");
        }
        toggleRefundField();
    }

    private void selectSeat(int seatId) {
        selectComboItem(seatCombo, seat -> seat.getSeatId() == seatId);
    }

    private void selectEvent(int eventId) {
        selectComboItem(eventCombo, event -> event.getEventId() == eventId);
        refreshMatchOptions();
    }

    private void selectCustomer(int customerId) {
        selectComboItem(customerCombo, customer -> customer.getCustomerId() == customerId);
    }

    private void selectTicket(int ticketId) {
        selectComboItem(ticketCombo, ticket -> ticket.getTicketId() == ticketId);
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
        recordIdField.setText("");
        if (seatCombo.getItemCount() > 0) seatCombo.setSelectedIndex(0);
        if (eventCombo.getItemCount() > 0) eventCombo.setSelectedIndex(0);
        if (customerCombo.getItemCount() > 0) customerCombo.setSelectedIndex(0);
        if (ticketCombo.getItemCount() > 0) ticketCombo.setSelectedIndex(0);
        saleDatetimeField.setText("");
        priceField.setText("");
        matchCombo.setSelectedIndex(0);
        statusCombo.setSelectedItem("Sold");
        refundDatetimeField.setText("");
        toggleRefundField();
        table.clearSelection();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Seat and Ticket Sale"));

        addFormField(panel, 0, 0, "Record ID (auto)", recordIdField);
        addFormField(panel, 0, 1, "Seat", seatCombo);
        addFormField(panel, 1, 0, "Event", eventCombo);
        addFormField(panel, 1, 1, "Customer", customerCombo);
        addFormField(panel, 2, 0, "Sale Timestamp", saleDatetimeField);
        addFormField(panel, 2, 1, "Price Sold", priceField);
        addFormField(panel, 3, 0, "Ticket", ticketCombo);
        addFormField(panel, 3, 1, "Match (optional)", matchCombo);
        addFormField(panel, 4, 0, "Sale Status", statusCombo);
        addFormField(panel, 4, 1, "Refund Timestamp", refundDatetimeField);

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
