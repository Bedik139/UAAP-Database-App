import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CustomerManagerPanel extends JPanel {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private JTable customerTable;
    private DefaultTableModel customerTableModel;
    private JTable purchaseTable;
    private DefaultTableModel purchaseTableModel;

    public CustomerManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(UAAPTheme.LIGHT_SURFACE);

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);

        // Top panel - Customers
        JPanel customerPanel = createCustomerPanel();
        splitPane.setTopComponent(customerPanel);

        // Bottom panel - Ticket Purchases
        JPanel purchasePanel = createPurchasePanel();
        splitPane.setBottomComponent(purchasePanel);

        add(splitPane, BorderLayout.CENTER);

        // Load initial data
        loadCustomers();
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Customer List"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Customer table
        customerTableModel = new DefaultTableModel(
                new Object[]{"ID", "First Name", "Last Name", "Email", "Phone", "Preferred Team", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(customerTableModel);
        UAAPTheme.styleTable(customerTable);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Selection listener
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = customerTable.getSelectedRow();
                if (row >= 0) {
                    int customerId = (Integer) customerTableModel.getValueAt(row, 0);
                    loadCustomerPurchases(customerId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(customerTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton refreshButton = new JButton("Refresh");
        UAAPTheme.styleInfoButton(refreshButton);
        refreshButton.addActionListener(e -> loadCustomers());
        
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPurchasePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Ticket Purchase History"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Purchase table
        purchaseTableModel = new DefaultTableModel(
                new Object[]{"Ticket ID", "Event Name", "Sport", "Match ID", "Seat Type", "Quantity", "Total Price", "Purchase Date", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        purchaseTable = new JTable(purchaseTableModel);
        UAAPTheme.styleTable(purchaseTable);

        JScrollPane scrollPane = new JScrollPane(purchaseTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel totalTicketsLabel = new JLabel("Total Tickets: 0", SwingConstants.CENTER);
        JLabel totalSpentLabel = new JLabel("Total Spent: Php0.00", SwingConstants.CENTER);
        JLabel activeTicketsLabel = new JLabel("Active Tickets: 0", SwingConstants.CENTER);

        totalTicketsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalSpentLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        activeTicketsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        statsPanel.add(totalTicketsLabel);
        statsPanel.add(totalSpentLabel);
        statsPanel.add(activeTicketsLabel);

        panel.add(statsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadCustomers() {
        customerTableModel.setRowCount(0);
        try {
            var customers = customerDAO.getAllCustomers();
            for (Customer customer : customers) {
                customerTableModel.addRow(new Object[]{
                        customer.getCustomerId(),
                        customer.getFirstName(),
                        customer.getLastName(),
                        customer.getEmail(),
                        customer.getPhoneNumber(),
                        customer.getPreferredTeam(),
                        customer.getStatus()
                });
            }
        } catch (SQLException ex) {
            showError("Unable to load customers:\n" + ex.getMessage());
        }
    }

    private void loadCustomerPurchases(int customerId) {
        purchaseTableModel.setRowCount(0);
        
        String sql = "SELECT t.ticket_id, e.event_name, e.sport, sat.match_id, s.seat_type, " +
                "sat.quantity, sat.total_price, sat.sale_datetime, sat.sale_status " +
                "FROM ticket t " +
                "INNER JOIN seat_and_ticket sat ON t.seat_and_ticket_id = sat.seat_and_ticket_id " +
                "INNER JOIN event e ON sat.event_id = e.event_id " +
                "INNER JOIN seat s ON sat.seat_id = s.seat_id " +
                "WHERE t.customer_id = ? " +
                "ORDER BY sat.sale_datetime DESC";

        int totalTickets = 0;
        double totalSpent = 0.0;
        int activeTickets = 0;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, customerId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("total_price");
                    String status = rs.getString("sale_status");
                    
                    purchaseTableModel.addRow(new Object[]{
                            rs.getInt("ticket_id"),
                            rs.getString("event_name"),
                            rs.getString("sport"),
                            rs.getObject("match_id") != null ? rs.getInt("match_id") : "N/A",
                            rs.getString("seat_type"),
                            quantity,
                            String.format("Php %.2f", price),
                            rs.getTimestamp("sale_datetime"),
                            status
                    });
                    
                    totalTickets += quantity;
                    if ("Sold".equals(status)) {
                        totalSpent += price;
                        activeTickets += quantity;
                    }
                }
            }

            // Update stats labels
            Component statsPanel = ((JPanel) purchaseTable.getParent().getParent()).getComponent(2);
            if (statsPanel instanceof JPanel) {
                JLabel totalTicketsLabel = (JLabel) ((JPanel) statsPanel).getComponent(0);
                JLabel totalSpentLabel = (JLabel) ((JPanel) statsPanel).getComponent(1);
                JLabel activeTicketsLabel = (JLabel) ((JPanel) statsPanel).getComponent(2);
                
                totalTicketsLabel.setText("Total Tickets: " + totalTickets);
                totalSpentLabel.setText(String.format("Total Spent: ₱%.2f", totalSpent));
                activeTicketsLabel.setText("Active Tickets: " + activeTickets);
            }

        } catch (SQLException ex) {
            showError("Unable to load customer purchases:\n" + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
