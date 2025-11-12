import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TicketRefundPanel extends JPanel {

    private final TicketingService ticketingService = new TicketingService();
    private final SeatAndTicketDAO seatAndTicketDAO = new SeatAndTicketDAO();
    private final EventDAO eventDAO = new EventDAO();

    private JComboBox<Event> eventFilterCombo;
    private DefaultTableModel tableModel;
    private JTable ticketTable;
    private List<SeatAndTicket> currentRecords = new ArrayList<>();

    private JTextField saleIdField;
    private JTextField reasonField;
    private JTextField processedByField;
    
    private JLabel totalTicketsLabel;
    private JLabel totalAmountLabel;

    public TicketRefundPanel() {
        setBackground(UAAPTheme.LIGHT_SURFACE);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);
        
        reloadEvents();
        reloadTable();
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 16));
        header.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel("Ticket Refund");
        titleLabel.setFont(UAAPTheme.TITLE_FONT);
        titleLabel.setForeground(UAAPTheme.PRIMARY_GREEN);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterPanel.setOpaque(false);

        JLabel eventLabel = new JLabel("Filter by Event:");
        eventLabel.setFont(UAAPTheme.TABLE_FONT);
        eventFilterCombo = new JComboBox<>();
        eventFilterCombo.setFont(UAAPTheme.TABLE_FONT);
        eventFilterCombo.setPreferredSize(new Dimension(250, 32));
        eventFilterCombo.addActionListener(e -> reloadTable());

        JButton refreshButton = new JButton("Refresh");
        UAAPTheme.styleNeutralButton(refreshButton);
        refreshButton.addActionListener(e -> {
            reloadEvents();
            reloadTable();
        });

        filterPanel.add(eventLabel);
        filterPanel.add(eventFilterCombo);
        filterPanel.add(refreshButton);

        header.add(titleLabel, BorderLayout.NORTH);
        header.add(filterPanel, BorderLayout.SOUTH);

        return header;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // Table
        tableModel = new DefaultTableModel(
                new Object[]{"Sale ID", "Event", "Seat Type", "Customer", "Sale Date", "Qty", "Unit Price", "Total Price"}, 0
        ) {
            @Override 
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
        };

        ticketTable = new JTable(tableModel);
        ticketTable.setFont(UAAPTheme.TABLE_FONT);
        ticketTable.setRowHeight(32);
        ticketTable.getTableHeader().setFont(UAAPTheme.TABLE_HEADER_FONT);
        ticketTable.getTableHeader().setBackground(UAAPTheme.TABLE_HEADER_BG);
        ticketTable.getTableHeader().setForeground(UAAPTheme.TEXT_PRIMARY);
        ticketTable.setSelectionBackground(UAAPTheme.SELECTION_BG);
        ticketTable.setGridColor(UAAPTheme.DIVIDER);
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Column widths
        ticketTable.getColumnModel().getColumn(0).setPreferredWidth(70);  // Sale ID
        ticketTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Event
        ticketTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Seat
        ticketTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Customer
        ticketTable.getColumnModel().getColumn(4).setPreferredWidth(140); // Date
        ticketTable.getColumnModel().getColumn(5).setPreferredWidth(50);  // Qty
        ticketTable.getColumnModel().getColumn(6).setPreferredWidth(90);  // Unit Price
        ticketTable.getColumnModel().getColumn(7).setPreferredWidth(90);  // Total

        ticketTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = ticketTable.getSelectedRow();
                if (row >= 0 && row < currentRecords.size()) {
                    SeatAndTicket record = currentRecords.get(row);
                    saleIdField.setText(String.valueOf(record.getRecordId()));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(ticketTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UAAPTheme.CARD_BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Summary Panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 8));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UAAPTheme.CARD_BORDER),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)
            )
        );

        totalTicketsLabel = new JLabel("Total Tickets: 0");
        totalTicketsLabel.setFont(UAAPTheme.SUBTITLE_FONT);
        totalTicketsLabel.setForeground(UAAPTheme.PRIMARY_GREEN);

        totalAmountLabel = new JLabel("Total Amount: ₱0.00");
        totalAmountLabel.setFont(UAAPTheme.SUBTITLE_FONT);
        totalAmountLabel.setForeground(UAAPTheme.ACCENT_GOLD);

        summaryPanel.add(totalTicketsLabel);
        summaryPanel.add(totalAmountLabel);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(false);
        outerPanel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UAAPTheme.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 0, 0, 0)
            )
        );

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JLabel formTitle = new JLabel("Refund Details");
        formTitle.setFont(UAAPTheme.SUBTITLE_FONT);
        formTitle.setForeground(UAAPTheme.PRIMARY_GREEN);

        saleIdField = new JTextField(10);
        saleIdField.setFont(UAAPTheme.TABLE_FONT);
        saleIdField.setPreferredSize(new Dimension(150, 32));
        
        reasonField = new JTextField(30);
        reasonField.setFont(UAAPTheme.TABLE_FONT);
        reasonField.setPreferredSize(new Dimension(300, 32));
        
        processedByField = new JTextField(20);
        processedByField.setFont(UAAPTheme.TABLE_FONT);
        processedByField.setPreferredSize(new Dimension(200, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Title row
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        panel.add(formTitle, gbc);

        // Sale ID
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel saleIdLabel = new JLabel("Sale ID:");
        saleIdLabel.setFont(UAAPTheme.TABLE_FONT);
        panel.add(saleIdLabel, gbc);

        gbc.gridx = 1;
        panel.add(saleIdField, gbc);

        // Reason
        gbc.gridx = 2;
        JLabel reasonLabel = new JLabel("Reason:");
        reasonLabel.setFont(UAAPTheme.TABLE_FONT);
        panel.add(reasonLabel, gbc);

        gbc.gridx = 3;
        panel.add(reasonField, gbc);

        // Processed By
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel processedByLabel = new JLabel("Processed By:");
        processedByLabel.setFont(UAAPTheme.TABLE_FONT);
        panel.add(processedByLabel, gbc);

        gbc.gridx = 1;
        panel.add(processedByField, gbc);

        // Buttons
        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.EAST;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton clearButton = new JButton("Clear");
        UAAPTheme.styleNeutralButton(clearButton);
        clearButton.addActionListener(e -> clearForm());

        JButton refundButton = new JButton("Issue Refund");
        UAAPTheme.styleActionButton(refundButton);
        refundButton.addActionListener(e -> handleRefund());

        buttonPanel.add(clearButton);
        buttonPanel.add(refundButton);

        panel.add(buttonPanel, gbc);

        outerPanel.add(panel, BorderLayout.CENTER);
        return outerPanel;
    }

    private void clearForm() {
        saleIdField.setText("");
        reasonField.setText("");
        processedByField.setText("");
        ticketTable.clearSelection();
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

        int totalCount = 0;
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

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
                totalCount++;
                totalAmount = totalAmount.add(record.getTotalPrice());
                
                tableModel.addRow(new Object[]{
                        record.getRecordId(),
                        record.getEventName(),
                        record.getSeatLabel(),
                        record.getCustomerName(),
                        record.getSaleDatetime(),
                        record.getQuantity(),
                        formatMoney(record.getUnitPrice()),
                        formatMoney(record.getTotalPrice())
                });
            }

            // Update summary
            totalTicketsLabel.setText("Total Tickets: " + totalCount);
            totalAmountLabel.setText("Total Amount: ₱" + formatMoney(totalAmount));

        } catch (SQLException ex) {
            showError("Unable to load ticket sales:\n" + ex.getMessage());
        }
    }

    private void handleRefund() {
        if (saleIdField.getText().trim().isEmpty()) {
            showError("Please select a sale from the table or enter the sale ID.");
            return;
        }

        try {
            int saleId = Integer.parseInt(saleIdField.getText().trim());
            String reason = reasonField.getText().trim();
            String processedBy = processedByField.getText().trim().isEmpty()
                    ? "Customer Portal"
                    : processedByField.getText().trim();

            // Confirmation dialog
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to process this refund?",
                    "Confirm Refund",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            TicketingService.TicketRefundResult result = ticketingService.refundTicket(
                    saleId,
                    reason.isEmpty() ? "Customer initiated refund." : reason,
                    processedBy
            );

            // Show success message with styled dialog
            String message = String.format(
                    "<html><body style='width: 300px; font-family: Segoe UI; padding: 10px;'>" +
                    "<h3 style='color: #4CAF50; margin-bottom: 10px;'>✓ Refund Processed Successfully</h3>" +
                    "<p><b>Sale ID:</b> %d</p>" +
                    "<p><b>Refund Amount:</b> ₱%s</p>" +
                    "<p><b>Customer ID:</b> %d</p>" +
                    "<p><b>Timestamp:</b> %s</p>" +
                    "</body></html>",
                    result.saleRecordId(),
                    result.amountRefunded().toPlainString(),
                    result.customerId(),
                    result.refundTimestamp()
            );

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Refund Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            reloadTable();
            
        } catch (NumberFormatException ex) {
            showError("Invalid Sale ID format. Please enter a valid number.");
        } catch (Exception ex) {
            showError("Unable to process refund:\n" + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private String formatMoney(java.math.BigDecimal value) {
        if (value == null) return "0.00";
        return String.format("%,.2f", value);
    }
}
