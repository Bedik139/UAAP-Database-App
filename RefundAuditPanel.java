import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RefundAuditPanel extends JPanel {

    private final RefundAuditDAO refundAuditDAO = new RefundAuditDAO();
    private DefaultTableModel tableModel;
    private JTable refundTable;
    private JTextField searchField;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JLabel totalRefundsLabel;
    private JLabel totalAmountLabel;

    public RefundAuditPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UAAPTheme.LIGHT_SURFACE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Summary Panel
        JPanel summaryPanel = createSummaryPanel();
        add(summaryPanel, BorderLayout.SOUTH);

        // Load initial data
        loadRefundData(null, null, null);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel("Refund Audit Trail");
        titleLabel.setFont(UAAPTheme.TITLE_FONT);
        titleLabel.setForeground(UAAPTheme.PRIMARY_GREEN);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));

        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by customer name, email, or event");
        filterPanel.add(searchField);

        filterPanel.add(new JLabel("From Date:"));
        fromDateField = new JTextField(10);
        fromDateField.setToolTipText("Format: YYYY-MM-DD");
        filterPanel.add(fromDateField);

        filterPanel.add(new JLabel("To Date:"));
        toDateField = new JTextField(10);
        toDateField.setToolTipText("Format: YYYY-MM-DD");
        filterPanel.add(toDateField);

        JButton searchButton = new JButton("Search");
        UAAPTheme.styleActionButton(searchButton);
        searchButton.addActionListener(e -> performSearch());
        filterPanel.add(searchButton);

        JButton resetButton = new JButton("Reset");
        UAAPTheme.styleNeutralButton(resetButton);
        resetButton.addActionListener(e -> resetFilters());
        filterPanel.add(resetButton);

        panel.add(filterPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Create table model
        String[] columns = {
            "Audit ID",
            "Ticket ID",
            "Customer",
            "Email",
            "Event",
            "Seat Type",
            "Refund Amount",
            "Sale Date",
            "Refund Date",
            "Reason",
            "Processed By"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refundTable = new JTable(tableModel);
        refundTable.setFont(UAAPTheme.TABLE_FONT);
        refundTable.setRowHeight(30);
        refundTable.getTableHeader().setFont(UAAPTheme.TABLE_HEADER_FONT);
        refundTable.getTableHeader().setBackground(UAAPTheme.TABLE_HEADER_BG);
        refundTable.getTableHeader().setForeground(UAAPTheme.TEXT_PRIMARY);
        refundTable.setSelectionBackground(UAAPTheme.SELECTION_BG);
        refundTable.setGridColor(UAAPTheme.DIVIDER);

        // Set column widths
        refundTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Audit ID
        refundTable.getColumnModel().getColumn(1).setPreferredWidth(70);  // Ticket ID
        refundTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Customer
        refundTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Email
        refundTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Event
        refundTable.getColumnModel().getColumn(5).setPreferredWidth(90);  // Seat Type
        refundTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Refund Amount
        refundTable.getColumnModel().getColumn(7).setPreferredWidth(130); // Sale Date
        refundTable.getColumnModel().getColumn(8).setPreferredWidth(130); // Refund Date
        refundTable.getColumnModel().getColumn(9).setPreferredWidth(250); // Reason
        refundTable.getColumnModel().getColumn(10).setPreferredWidth(150); // Processed By

        JScrollPane scrollPane = new JScrollPane(refundTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UAAPTheme.CARD_BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UAAPTheme.CARD_BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        totalRefundsLabel = new JLabel("Total Refunds: 0");
        totalRefundsLabel.setFont(UAAPTheme.SUBTITLE_FONT);
        totalRefundsLabel.setForeground(UAAPTheme.PRIMARY_GREEN);

        totalAmountLabel = new JLabel("Total Amount: ₱0.00");
        totalAmountLabel.setFont(UAAPTheme.SUBTITLE_FONT);
        totalAmountLabel.setForeground(UAAPTheme.ACCENT_GOLD);

        panel.add(totalRefundsLabel);
        panel.add(totalAmountLabel);

        return panel;
    }

    private void loadRefundData(String searchQuery, Date fromDate, Date toDate) {
        tableModel.setRowCount(0);

        try {
            List<RefundAuditDAO.RefundAuditDetail> refunds;

            if (searchQuery != null || fromDate != null || toDate != null) {
                refunds = refundAuditDAO.searchRefunds(searchQuery, fromDate, toDate);
            } else {
                refunds = refundAuditDAO.getAllRefundsWithDetails();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            double totalAmount = 0.0;

            for (RefundAuditDAO.RefundAuditDetail refund : refunds) {
                Object[] row = {
                    refund.getAuditId(),
                    refund.getSeatAndTicketRecId(),
                    refund.getCustomerName(),
                    refund.getCustomerEmail(),
                    refund.getEventName(),
                    refund.getSeatType(),
                    String.format("₱%.2f", refund.getRefundAmount()),
                    dateFormat.format(refund.getSaleDateTime()),
                    dateFormat.format(refund.getRefundDatetime()),
                    refund.getReason() != null ? refund.getReason() : "N/A",
                    refund.getProcessedBy() != null ? refund.getProcessedBy() : "System"
                };
                tableModel.addRow(row);
                totalAmount += refund.getRefundAmount().doubleValue();
            }

            // Update summary
            totalRefundsLabel.setText("Total Refunds: " + refunds.size());
            totalAmountLabel.setText(String.format("Total Amount: ₱%.2f", totalAmount));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading refund data:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    private void performSearch() {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            searchQuery = null;
        }

        Date fromDate = null;
        Date toDate = null;

        try {
            String fromDateText = fromDateField.getText().trim();
            if (!fromDateText.isEmpty()) {
                fromDate = Date.valueOf(fromDateText);
            }

            String toDateText = toDateField.getText().trim();
            if (!toDateText.isEmpty()) {
                toDate = Date.valueOf(toDateText);
            }

            loadRefundData(searchQuery, fromDate, toDate);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Invalid date format. Please use YYYY-MM-DD format.",
                "Input Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void resetFilters() {
        searchField.setText("");
        fromDateField.setText("");
        toDateField.setText("");
        loadRefundData(null, null, null);
    }

    public void refreshData() {
        resetFilters();
    }
}
