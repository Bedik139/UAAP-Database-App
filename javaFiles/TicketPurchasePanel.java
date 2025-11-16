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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TicketPurchasePanel extends JPanel {

    private final EventDAO eventDAO = new EventDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TicketingService ticketingService = new TicketingService();
    private final TeamDAO teamDAO = new TeamDAO();

    private JComboBox<Event> eventCombo;
    private JComboBox<Match> matchCombo;
    private JComboBox<Seat> seatCombo;
    private JComboBox<Ticket> ticketCombo;
    private JComboBox<Customer> customerCombo;

    private JTextField quantityField;
    private JTextField unitPriceField;
    private JTextField saleDateTimeField;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField organizationField;
    private JComboBox<Object> preferredTeamCombo;
    private JTextField paymentMethodField;

    private JTextArea confirmationArea;

    public TicketPurchasePanel() {
        setLayout(new BorderLayout(10, 10));

        JPanel formStack = new JPanel();
        formStack.setOpaque(false);
        formStack.setLayout(new BoxLayout(formStack, BoxLayout.Y_AXIS));
        JPanel selectionPanel = buildSelectionPanel();
        selectionPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        JPanel customerPanel = buildCustomerPanel();
        customerPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        formStack.add(selectionPanel);
        formStack.add(Box.createVerticalStrut(14));
        formStack.add(customerPanel);

        JScrollPane formScroll = new JScrollPane(formStack);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(formScroll, BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);
        reloadAllData();
    }

    private JPanel buildSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Ticket Selection"));
        panel.setBackground(UAAPTheme.CARD_BACKGROUND);

        eventCombo = new JComboBox<>();
        UAAPTheme.styleComboBox(eventCombo);
        eventCombo.addActionListener(e -> refreshMatchesAndSeats());

        matchCombo = new JComboBox<>();
        UAAPTheme.styleComboBox(matchCombo);

        seatCombo = new JComboBox<>();
        UAAPTheme.styleComboBox(seatCombo);
        seatCombo.addActionListener(e -> syncSeatTicketSelection());
        
        ticketCombo = new JComboBox<>();
        UAAPTheme.styleComboBox(ticketCombo);
        ticketCombo.setEnabled(false);
        ticketCombo.setToolTipText("Ticket tier is derived from the selected seat.");

        quantityField = new JTextField("1");
        quantityField.setToolTipText("Customers may purchase between 1 and 2 seats per transaction.");
        UAAPTheme.styleTextField(quantityField);
        
        unitPriceField = new JTextField(10);
        unitPriceField.setEditable(false);
        unitPriceField.setToolTipText("Read-only seat price from the linked ticket.");
        UAAPTheme.styleTextField(unitPriceField);
        
        saleDateTimeField = new JTextField(16);
        saleDateTimeField.setToolTipText("Optional override (YYYY-MM-DD HH:MM:SS). Leave blank for current time.");
        UAAPTheme.styleTextField(saleDateTimeField);

        addFormField(panel, 0, "Event", eventCombo);
        addFormField(panel, 1, "Match (Scheduled)", matchCombo);
        addFormField(panel, 2, "Available Seat", seatCombo);
        addFormField(panel, 3, "Ticket Tier", ticketCombo);
        addFormField(panel, 4, "Seat Price", unitPriceField);
        addFormField(panel, 5, "Quantity", quantityField);
        addFormField(panel, 6, "Sale Timestamp", saleDateTimeField);

        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel buildCustomerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        panel.setBackground(UAAPTheme.CARD_BACKGROUND);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        customerCombo = new JComboBox<>();
        UAAPTheme.styleComboBox(customerCombo);
        customerCombo.addActionListener(e -> toggleCustomerFields());

        firstNameField = new JTextField(15);
        UAAPTheme.styleTextField(firstNameField);
        
        lastNameField = new JTextField(15);
        UAAPTheme.styleTextField(lastNameField);
        
        emailField = new JTextField(20);
        UAAPTheme.styleTextField(emailField);
        
        phoneField = new JTextField(15);
        UAAPTheme.styleTextField(phoneField);
        
        organizationField = new JTextField(20);
        UAAPTheme.styleTextField(organizationField);
        
        preferredTeamCombo = new JComboBox<>();
        UAAPTheme.styleComboBox(preferredTeamCombo);
        preferredTeamCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list,
                                                                   Object value,
                                                                   int index,
                                                                   boolean isSelected,
                                                                   boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Team) {
                    setText(((Team) value).getTeamName());
                } else if (value == null) {
                    setText("None");
                } else {
                    setText(value.toString());
                }
                return this;
            }
        });
        
        paymentMethodField = new JTextField(15);
        UAAPTheme.styleTextField(paymentMethodField);

        addFormField(panel, 0, "Existing Customer (optional)", customerCombo);
        addFormField(panel, 1, "First Name", firstNameField);
        addFormField(panel, 1, "Last Name", lastNameField, 1);
        addFormField(panel, 2, "Email", emailField);
        addFormField(panel, 2, "Phone", phoneField, 1);
        addFormField(panel, 3, "Organization", organizationField);
        addFormField(panel, 3, "Preferred Team", preferredTeamCombo, 1);
        addFormField(panel, 4, "Payment Method", paymentMethodField);

        return panel;
    }

    private JPanel buildFooterPanel() {
        JButton purchaseButton = new JButton("Purchase Ticket");
        JButton clearButton = new JButton("Clear Form");
        JButton reloadButton = new JButton("Reload Data");

        UAAPTheme.styleActionButton(purchaseButton);
        UAAPTheme.styleNeutralButton(clearButton);
        UAAPTheme.styleInfoButton(reloadButton);

        purchaseButton.addActionListener(e -> handlePurchase());
        clearButton.addActionListener(e -> clearForm());
        reloadButton.addActionListener(e -> reloadAllData());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setOpaque(false);
        actionPanel.add(reloadButton);
        actionPanel.add(clearButton);
        actionPanel.add(purchaseButton);

        confirmationArea = new JTextArea(4, 70);
        confirmationArea.setEditable(false);
        confirmationArea.setLineWrap(true);
        confirmationArea.setWrapStyleWord(true);
        confirmationArea.setFont(UAAPTheme.LABEL_FONT);
        confirmationArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UAAPTheme.CARD_BORDER, 1),
                "Last Transaction",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new java.awt.Font("Segoe UI Semibold", java.awt.Font.PLAIN, 14),
                UAAPTheme.TEXT_PRIMARY
            ),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
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
        reloadTeamOptions();
        reloadCustomers();
        refreshMatchesAndSeats();
        applyDefaultQuantity();
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
                for (Match match : matches) {
                    if (match.getEventId() == event.getEventId() && "Scheduled".equalsIgnoreCase(match.getStatus())) {
                        matchModel.addElement(match);
                    }
                }
            } catch (SQLException ex) {
                showError("Unable to load matches:\n" + ex.getMessage());
            }

            try {
                for (Seat seat : seatDAO.getAvailableSeatsForEvent(event.getEventId(), event.getVenueAddress())) {
                    seatModel.addElement(seat);
                }
            } catch (SQLException ex) {
                showError("Unable to load seats:\n" + ex.getMessage());
            }
        }

        matchCombo.setModel(matchModel);
        matchCombo.setSelectedIndex(matchModel.getSize() > 0 ? 0 : -1);
        seatCombo.setModel(seatModel);
        applyDefaultQuantity();
        syncSeatTicketSelection();
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

    private void reloadTeamOptions() {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();
        model.addElement("None");
        try {
            for (Team team : teamDAO.getAllTeams()) {
                model.addElement(team);
            }
        } catch (SQLException ex) {
            showError("Unable to load teams:\n" + ex.getMessage());
        }
        preferredTeamCombo.setModel(model);
        preferredTeamCombo.setSelectedIndex(0);
    }

    private void applyDefaultQuantity() {
        quantityField.setText("1");
    }

    private void toggleCustomerFields() {
        Customer existing = (Customer) customerCombo.getSelectedItem();
        boolean isNew = existing == null;
        firstNameField.setEnabled(isNew);
        lastNameField.setEnabled(isNew);
        emailField.setEnabled(isNew);
        phoneField.setEnabled(isNew);
        organizationField.setEnabled(isNew);
        preferredTeamCombo.setEnabled(isNew);
        paymentMethodField.setEnabled(true);

        if (!isNew && existing != null) {
            firstNameField.setText(existing.getFirstName());
            lastNameField.setText(existing.getLastName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhoneNumber());
            organizationField.setText(existing.getOrganization());
            selectPreferredTeam(existing.getPreferredTeam());
            paymentMethodField.setText(existing.getPaymentMethod() != null ? existing.getPaymentMethod() : "");
        } else {
            firstNameField.setText("");
            lastNameField.setText("");
            emailField.setText("");
            phoneField.setText("");
            organizationField.setText("");
            preferredTeamCombo.setSelectedIndex(0);
            paymentMethodField.setText("");
        }
    }

    private void selectPreferredTeam(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            preferredTeamCombo.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < preferredTeamCombo.getItemCount(); i++) {
            Object item = preferredTeamCombo.getItemAt(i);
            if (item instanceof Team && teamName.equals(((Team) item).getTeamName())) {
                preferredTeamCombo.setSelectedIndex(i);
                return;
            }
        }
        preferredTeamCombo.setSelectedIndex(0);
    }

    private String resolvePreferredTeamSelection() {
        Object selection = preferredTeamCombo.getSelectedItem();
        if (selection instanceof Team) {
            return ((Team) selection).getTeamName();
        }
        return null;
    }

    private void handlePurchase() {
        Event event = (Event) eventCombo.getSelectedItem();
        Seat seat = (Seat) seatCombo.getSelectedItem();
        if (event == null || seat == null) {
            showError("Event and seat selections are required.");
            return;
        }

        try {
            Match selectedMatch = (Match) matchCombo.getSelectedItem();
            if (selectedMatch == null) {
                showError("Select a match for this ticket.");
                return;
            }
            Ticket seatTicket = seat.getTicketTier();
            if (seatTicket == null) {
                showError("Selected seat is not linked to a ticket tier.");
                return;
            }
            int quantity = parseQuantity(quantityField);

            TicketingService.TicketPurchaseRequest request = new TicketingService.TicketPurchaseRequest();
            request.setEventId(event.getEventId());
            request.setMatchId(selectedMatch.getMatchId());
            request.setSeatId(seat.getSeatId());
            request.setTicketId(seatTicket.getTicketId());
            request.setQuantity(quantity);

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
                request.setPreferredTeam(resolvePreferredTeamSelection());
                request.setCustomerStatus("Active");
                request.setPaymentMethod(paymentMethodField.getText().trim());
            }

            TicketingService.TicketPurchaseResult result = ticketingService.purchaseTicket(request);
            confirmationArea.setText(String.format(
                    "Purchase successful!%nSale ID: %d%nEvent: %s%nSeat: %s%nQuantity: %d%nUnit Price: %s%nTotal Amount: %s%nTimestamp: %s",
                    result.saleRecordId(),
                    result.event().getEventName(),
                    result.seat().toString(),
                    result.quantity(),
                    formatMoney(result.unitPrice()),
                    formatMoney(result.totalAmount()),
                    result.saleTimestamp()
            ));

            reloadCustomers();
            refreshMatchesAndSeats();
        } catch (Exception ex) {
            showError("Unable to complete purchase:\n" + ex.getMessage());
        }
    }

    private void validateNewCustomerFields() {
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        if (first.isEmpty() || last.isEmpty()) {
            throw new IllegalArgumentException("First and last name are required for new customers.");
        }
        if (email.isEmpty() && phone.isEmpty()) {
            throw new IllegalArgumentException("Provide at least one contact detail (email or phone) for the new customer.");
        }
    }

    private int parseQuantity(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Quantity is required.");
        }
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                throw new IllegalArgumentException("Quantity must be at least 1.");
            }
            if (value > 2) {
                throw new IllegalArgumentException("You may purchase at most 2 seats per transaction.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Quantity must be a whole number.");
        }
    }

    private void syncSeatTicketSelection() {
        Seat seat = (Seat) seatCombo.getSelectedItem();
        if (seat == null) {
            ticketCombo.setSelectedIndex(-1);
            unitPriceField.setText("");
            return;
        }
        selectTicketInCombo(seat.getTicketId());
        unitPriceField.setText(formatMoney(seat.getTicketPrice()));
    }

    private void selectTicketInCombo(int ticketId) {
        ComboBoxModel<Ticket> model = ticketCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Ticket ticket = model.getElementAt(i);
            if (ticket != null && ticket.getTicketId() == ticketId) {
                ticketCombo.setSelectedIndex(i);
                return;
            }
        }
        ticketCombo.setSelectedIndex(-1);
    }

    private String formatMoney(BigDecimal value) {
        return value != null ? value.stripTrailingZeros().toPlainString() : "";
    }

    private void clearForm() {
        customerCombo.setSelectedIndex(0);
        matchCombo.setSelectedIndex(0);
        seatCombo.setSelectedIndex(seatCombo.getItemCount() > 0 ? 0 : -1);
        ticketCombo.setSelectedIndex(ticketCombo.getItemCount() > 0 ? 0 : -1);
        saleDateTimeField.setText("");
        quantityField.setText("1");
        confirmationArea.setText("");
        toggleCustomerFields();
        applyDefaultQuantity();
        syncSeatTicketSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
