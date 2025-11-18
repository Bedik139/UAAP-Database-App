import java.awt.*;
import javax.swing.*;

public class TicketRefundPanel extends JPanel {

    private final TicketingService ticketingService = new TicketingService();

    private JTextField saleIdField;
    private JTextField reasonField;
    private JTextField processedByField;

    public TicketRefundPanel() {
        setBackground(UAAPTheme.LIGHT_SURFACE);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildActionPanel(), BorderLayout.CENTER);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 16));
        header.setOpaque(false);

        // Title
        JLabel titleLabel = new JLabel("Ticket Refund");
        titleLabel.setFont(UAAPTheme.TITLE_FONT);
        titleLabel.setForeground(UAAPTheme.PRIMARY_GREEN);

        header.add(titleLabel, BorderLayout.CENTER);

        return header;
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
        saleIdField.setToolTipText("Enter your Sale ID from purchase confirmation");
        
        reasonField = new JTextField(30);
        reasonField.setFont(UAAPTheme.TABLE_FONT);
        reasonField.setPreferredSize(new Dimension(300, 32));
        reasonField.setToolTipText("Enter reason for refund (optional)");
        
        processedByField = new JTextField(20);
        processedByField.setFont(UAAPTheme.TABLE_FONT);
        processedByField.setPreferredSize(new Dimension(200, 32));
        processedByField.setText("UAAP Company");
        processedByField.setEditable(false);
        processedByField.setToolTipText("Auto-filled for UAAP Company refund processing");

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
        processedByField.setText("UAAP Company");
    }

    private void handleRefund() {
        if (saleIdField.getText().trim().isEmpty()) {
            showError("Please enter your Sale ID.");
            return;
        }

        try {
            int saleId = Integer.parseInt(saleIdField.getText().trim());
            String reason = reasonField.getText().trim();
            String processedBy = processedByField.getText().trim().isEmpty()
                    ? "UAAP Company"
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
}
