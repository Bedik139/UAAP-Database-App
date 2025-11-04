import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class TicketPurchasePanel extends JPanel {

    private final EventDAO eventDAO = new EventDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TicketingService ticketingService = new TicketingService();

    private JComboBox<Event> eventCombo;
    private JComboBox<Match> matchCombo;
    private JComboBox<Seat> seatCombo;
    private JComboBox<Ticket> ticketCombo;
    private JComboBox<Customer> customerCombo;

    private JTextField priceField;
    private JTextField saleDateTimeField;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField organizationField;
    private JComboBox<String> preferredSportCombo;
    private JTextField paymentMethodField;

    private JTextArea confirmationArea;

    public TicketPurchasePanel() {
        setLayout(new BorderLayout(10, 10));
        add(buildSelectionPanel(), BorderLayout.NORTH);
        add(buildCustomerPanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);
        reloadAllData();
    }

    private JPanel buildSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Ticket Selection"));

        eventCombo = new JComboBox<>();
        eventCombo.addActionListener(e -> refreshMatchesAndSeats());

        matchCombo = new JComboBox<>();

        seatCombo = new JComboBox<>();
        ticketCombo = new JComboBox<>();
        ticketCombo.addActionListener(e -> applyTicketPrice());

        priceField = new JTextField(10);
        saleDateTimeField = new JTextField(16);
        saleDateTimeField.setToolTipText("Optional override (YYYY-MM-DD HH:MM:SS). Leave blank for current time.");

        addFormField(panel, 0, "Event", eventCombo);
        addFormField(panel, 1, "Match (Scheduled)", matchCombo);
        addFormField(panel, 2, "Available Seat", seatCombo);
        addFormField(panel, 3, "Ticket Type", ticketCombo);
        addFormField(panel, 4, "Sale Price", priceField);
        addFormField(panel, 5, "Sale Timestamp", saleDateTimeField);

        return panel;
    }

    private JPanel buildCustomerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Customer Details"));

        customerCombo = new JComboBox<>();
        customerCombo.addActionListener(e -> toggleCustomerFields());

        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(20);
        phoneField = new JTextField(15);
        organizationField = new JTextField(20);
        preferredSportCombo = new JComboBox<>(new String[]{"Basketball", "Volleyball", "None"});
        paymentMethodField = new JTextField(15);

        addFormField(panel, 0, "Existing Customer (optional)", customerCombo);
        addFormField(panel, 1, "First Name", firstNameField);
        addFormField(panel, 1, "Last Name", lastNameField, 1);
        addFormField(panel, 2, "Email", emailField);
        addFormField(panel, 2, "Phone", phoneField, 1);
        addFormField(panel, 3, "Organization", organizationField);
        addFormField(panel, 3, "Preferred Sport", preferredSportCombo, 1);
        addFormField(panel, 4, "Payment Method", paymentMethodField);

        return panel;
    }

    private JPanel buildFooterPanel() {
        JButton purchaseButton = new JButton("Purchase Ticket");
        JButton clearButton = new JButton("Clear Form");
        JButton reloadButton = new JButton("Reload Data");

        purchaseButton.addActionListener(e -> handlePurchase());
        clearButton.addActionListener(e -> clearForm());
        reloadButton.addActionListener(e -> reloadAllData());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(reloadButton);
        actionPanel.add(clearButton);
        actionPanel.add(purchaseButton);

        confirmationArea = new JTextArea(4, 70);
        confirmationArea.setEditable(false);
        confirmationArea.setLineWrap(true);
        confirmationArea.setWrapStyleWord(true);
        confirmationArea.setBorder(BorderFactory.createTitledBorder("Last Transaction"));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(actionPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(confirmationArea), BorderLayout.CENTER);
        return panel;
    }

    private void addFormField(JPanel panel, int row, String label, java.awt.Component component) {
        addFormField(panel, row, label, component, 0);
    }

    private void addFormField(JPanel panel, int row, String label, java.awt.Component component, int colOffset) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = colOffset * 2;
        labelGbc.gridy = row;
        labelGbc.insets = new Insets(6, 6, 6, 6);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = colOffset * 2 + 1;
        fieldGbc.gridy = row;
        fieldGbc.insets = new Insets(6, 6, 6, 6);
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1;
        panel.add(component, fieldGbc);
    }

    private void reloadAllData() {
        reloadEvents();
        reloadTickets();
        reloadCustomers();
        refreshMatchesAndSeats();
        applyTicketPrice();
        toggleCustomerFields();
        confirmationArea.setText("");
    }

    private void reloadEvents() {
        try {
            List<Event> events = eventDAO.getAllEvents();
            DefaultComboBoxModel<Event> model = new DefaultComboBoxModel<>();
            for (Event event : events) {
                if (!"Cancelled".equalsIgnoreCase(event.getEventStatus())) {
                    model.addElement(event);
                }
            }
            eventCombo.setModel(model);
            if (model.getSize() > 0) {
                eventCombo.setSelectedIndex(0);
            }
        } catch (SQLException ex) {
            showError("Unable to load events:\n" + ex.getMessage());
        }
    }

    private void refreshMatchesAndSeats() {
        Event event = (Event) eventCombo.getSelectedItem();
        DefaultComboBoxModel<Match> matchModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<Seat> seatModel = new DefaultComboBoxModel<>();

        if (event != null) {
            try {
                List<Match> matches = matchDAO.getAllMatches();
                matchModel.addElement(null);
                for (Match match : matches) {
                    if (match.getEventId() == event.getEventId() && "Scheduled".equalsIgnoreCase(match.getStatus())) {
                        matchModel.addElement(match);
                    }
                }
            } catch (SQLException ex) {
                showError("Unable to load matches:\n" + ex.getMessage());
            }

            try {
                for (Seat seat : seatDAO.getAvailableSeatsForEvent(event.getEventId())) {
                    seatModel.addElement(seat);
                }
            } catch (SQLException ex) {
                showError("Unable to load seats:\n" + ex.getMessage());
            }
        }

        matchCombo.setModel(matchModel);
        matchCombo.setSelectedIndex(0);
        seatCombo.setModel(seatModel);
    }

    private void reloadTickets() {
        try {
            DefaultComboBoxModel<Ticket> model = new DefaultComboBoxModel<>();
            for (Ticket ticket : ticketDAO.getAllTickets()) {
                if ("Active".equalsIgnoreCase(ticket.getStatus())) {
                    model.addElement(ticket);
                }
            }
            ticketCombo.setModel(model);
            if (model.getSize() > 0) {
                ticketCombo.setSelectedIndex(0);
            }
        } catch (SQLException ex) {
            showError("Unable to load tickets:\n" + ex.getMessage());
        }
    }

    private void reloadCustomers() {
        try {
            DefaultComboBoxModel<Customer> model = new DefaultComboBoxModel<>();
            model.addElement(null); // Represents new customer
            for (Customer customer : customerDAO.getAllCustomers()) {
                model.addElement(customer);
            }
            customerCombo.setModel(model);
            customerCombo.setSelectedIndex(0);
        } catch (SQLException ex) {
            showError("Unable to load customers:\n" + ex.getMessage());
        }
    }

    private void applyTicketPrice() {
        Ticket ticket = (Ticket) ticketCombo.getSelectedItem();
        if (ticket != null) {
            BigDecimal price = ticket.getPrice() != null ? ticket.getPrice() : ticket.getDefaultPrice();
            priceField.setText(price.toPlainString());
        }
    }

    private void toggleCustomerFields() {
        Customer existing = (Customer) customerCombo.getSelectedItem();
        boolean isNew = existing == null;
        firstNameField.setEnabled(isNew);
        lastNameField.setEnabled(isNew);
        emailField.setEnabled(isNew);
        phoneField.setEnabled(isNew);
        organizationField.setEnabled(isNew);
        preferredSportCombo.setEnabled(isNew);
        paymentMethodField.setEnabled(true);

        if (!isNew && existing != null) {
            firstNameField.setText(existing.getFirstName());
            lastNameField.setText(existing.getLastName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhoneNumber());
            organizationField.setText(existing.getOrganization());
            preferredSportCombo.setSelectedItem(existing.getPreferredSport() != null ? existing.getPreferredSport() : "None");
            paymentMethodField.setText(existing.getPaymentMethod() != null ? existing.getPaymentMethod() : "");
        } else {
            firstNameField.setText("");
            lastNameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            organizationField.setText("");
            preferredSportCombo.setSelectedIndex(0);
            paymentMethodField.setText("");
        }
    }

    private void handlePurchase() {
        Event event = (Event) eventCombo.getSelectedItem();
        Seat seat = (Seat) seatCombo.getSelectedItem();
        Ticket ticket = (Ticket) ticketCombo.getSelectedItem();

        if (event == null || seat == null || ticket == null) {
            showError("Event, seat, and ticket selections are required.");
            return;
        }

        try {
            Match selectedMatch = (Match) matchCombo.getSelectedItem();
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.signum() < 0) {
                throw new IllegalArgumentException("Price must be zero or greater.");
            }

            TicketingService.TicketPurchaseRequest request = new TicketingService.TicketPurchaseRequest();
            request.setEventId(event.getEventId());
            request.setMatchId(selectedMatch != null ? selectedMatch.getMatchId() : null);
            request.setSeatId(seat.getSeatId());
            request.setTicketId(ticket.getTicketId());
            request.setPriceOverride(price);

            String saleText = saleDateTimeField.getText().trim();
            if (!saleText.isEmpty()) {
                request.setSaleTimestamp(Timestamp.valueOf(saleText.replace('T', ' ')));
            }

            Customer existingCustomer = (Customer) customerCombo.getSelectedItem();
            if (existingCustomer != null) {
                request.setExistingCustomerId(existingCustomer.getCustomerId());
                request.setPaymentMethod(paymentMethodField.getText().trim().isEmpty()
                        ? existingCustomer.getPaymentMethod()
                        : paymentMethodField.getText().trim());
            } else {
                validateNewCustomerFields();
                request.setFirstName(firstNameField.getText().trim());
                request.setLastName(lastNameField.getText().trim());
                request.setEmail(emailField.getText().trim());
                request.setPhone(phoneField.getText().trim());
                request.setOrganization(organizationField.getText().trim());
                request.setRegistrationDate(Date.valueOf(LocalDate.now()));
                String preferred = "None".equals(preferredSportCombo.getSelectedItem())
                        ? null
                        : (String) preferredSportCombo.getSelectedItem();
                request.setPreferredSport(preferred);
                request.setCustomerStatus("Active");
                request.setPaymentMethod(paymentMethodField.getText().trim());
            }

            TicketingService.TicketPurchaseResult result = ticketingService.purchaseTicket(request);
            confirmationArea.setText(String.format(
                    "Purchase successful!%nSale ID: %d%nEvent: %s%nSeat: %s%nPrice: %s%nTimestamp: %s",
                    result.saleRecordId(),
                    result.event().getEventName(),
                    result.seat().toString(),
                    result.pricePaid().toPlainString(),
                    result.saleTimestamp()
            ));

            reloadCustomers();
            refreshMatchesAndSeats();
        } catch (Exception ex) {
            showError("Unable to complete purchase:\n" + ex.getMessage());
        }
    }

    private void validateNewCustomerFields() {
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("First name, last name, and email are required for new customers.");
        }
    }

    private void clearForm() {
        customerCombo.setSelectedIndex(0);
        matchCombo.setSelectedIndex(0);
        seatCombo.setSelectedIndex(seatCombo.getItemCount() > 0 ? 0 : -1);
        ticketCombo.setSelectedIndex(ticketCombo.getItemCount() > 0 ? 0 : -1);
        saleDateTimeField.setText("");
        priceField.setText("");
        confirmationArea.setText("");
        toggleCustomerFields();
        applyTicketPrice();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
