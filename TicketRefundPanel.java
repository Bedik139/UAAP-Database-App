import javax.swing.BorderFactory;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketRefundPanel extends JPanel {

    private final TicketingService ticketingService = new TicketingService();
    private final SeatAndTicketDAO seatAndTicketDAO = new SeatAndTicketDAO();
    private final EventDAO eventDAO = new EventDAO();

    private JComboBox<Event> eventFilterCombo;
    private DefaultTableModel tableModel;
    private JTable table;
    private List<SeatAndTicket> currentRecords = new ArrayList<>();

    private JTextField saleIdField;
    private JTextField reasonField;
    private JTextField processedByField;

    public TicketRefundPanel() {
        setLayout(new BorderLayout(10, 10));
        add(buildFilterPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);
        reloadEvents();
        reloadTable();
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Filter"));

        eventFilterCombo = new JComboBox<>();
        eventFilterCombo.addActionListener(e -> reloadTable());

        panel.add(new JLabel("Event:"));
        panel.add(eventFilterCombo);

        JButton refreshButton = new JButton("Reload");
        refreshButton.addActionListener(e -> {
            reloadEvents();
            reloadTable();
        });
        panel.add(refreshButton);

        return panel;
    }

    private JPanel buildTablePanel() {
        tableModel = new DefaultTableModel(
                new Object[]{"Sale ID", "Event", "Seat", "Customer", "Sold At", "Price"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < currentRecords.size()) {
                        SeatAndTicket record = currentRecords.get(row);
                        saleIdField.setText(String.valueOf(record.getRecordId()));
                    }
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Active Ticket Sales"));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Refund Details"));

        saleIdField = new JTextField(10);
        reasonField = new JTextField(25);
        processedByField = new JTextField(20);

        addFormField(panel, 0, "Sale ID", saleIdField);
        addFormField(panel, 1, "Reason", reasonField);
        addFormField(panel, 2, "Processed By", processedByField);

        JButton refundButton = new JButton("Issue Refund");
        refundButton.addActionListener(e -> handleRefund());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(refundButton, gbc);

        return panel;
    }

    private void addFormField(JPanel panel, int row, String label, JTextField field) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.insets = new Insets(6, 6, 6, 6);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.insets = new Insets(6, 6, 6, 6);
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1;
        panel.add(field, fieldGbc);
    }

    private void reloadEvents() {
        try {
            DefaultComboBoxModel<Event> model = new DefaultComboBoxModel<>();
            model.addElement(null);
            for (Event event : eventDAO.getAllEvents()) {
                model.addElement(event);
            }
            eventFilterCombo.setModel(model);
            eventFilterCombo.setSelectedIndex(0);
        } catch (SQLException ex) {
            showError("Unable to load events:\n" + ex.getMessage());
        }
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        currentRecords.clear();

        try {
            List<SeatAndTicket> all = seatAndTicketDAO.getAllRecords();
            Event filter = (Event) eventFilterCombo.getSelectedItem();
            for (SeatAndTicket record : all) {
                if (!record.isSold()) {
                    continue;
                }
                if (filter != null && record.getEventId() != filter.getEventId()) {
                    continue;
                }
                currentRecords.add(record);
                tableModel.addRow(new Object[]{
                        record.getRecordId(),
                        record.getEventName(),
                        record.getSeatLabel(),
                        record.getCustomerName(),
                        record.getSaleDatetime(),
                        record.getPriceSold()
                });
            }
        } catch (SQLException ex) {
            showError("Unable to load ticket sales:\n" + ex.getMessage());
        }
    }

    private void handleRefund() {
        if (saleIdField.getText().trim().isEmpty()) {
            showError("Select a sale from the table or enter the sale ID.");
            return;
        }

        try {
            int saleId = Integer.parseInt(saleIdField.getText().trim());
            String reason = reasonField.getText().trim();
            String processedBy = processedByField.getText().trim().isEmpty()
                    ? "Customer Portal"
                    : processedByField.getText().trim();

            TicketingService.TicketRefundResult result = ticketingService.refundTicket(
                    saleId,
                    reason.isEmpty() ? "Customer initiated refund." : reason,
                    processedBy
            );

            JOptionPane.showMessageDialog(
                    this,
                    String.format("Refund processed.%nRefund timestamp: %s%nAmount: %s",
                            result.refundTimestamp(),
                            result.amountRefunded()),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            saleIdField.setText("");
            reasonField.setText("");
            processedByField.setText("");
            reloadTable();
        } catch (Exception ex) {
            showError("Unable to process refund:\n" + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
