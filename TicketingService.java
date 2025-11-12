import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TicketingService {

    public TicketPurchaseResult purchaseTicket(TicketPurchaseRequest request) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                Event event = fetchEvent(conn, request.getEventId(), true);
                validateEventForSale(event);

                Match match = null;
                if (request.getMatchId() != null) {
                    match = fetchMatch(conn, request.getMatchId(), false);
                    if (match == null || match.getEventId() != request.getEventId()) {
                        throw new IllegalArgumentException("Selected match does not belong to the chosen event.");
                    }
                    validateMatchForSale(match);
                }

                Seat seat = fetchSeat(conn, request.getSeatId(), true);
                ensureSeatAvailable(conn, request.getEventId(), seat.getSeatId());
                Ticket seatTicket = seat.getTicketTier();
                if (seatTicket == null) {
                    throw new IllegalStateException("Seat is not linked to a ticket tier.");
                }

                int customerId = resolveCustomer(conn, request);
                if (request.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than zero.");
                }
                if (request.getQuantity() > 2) {
                    throw new IllegalArgumentException("Customers may only purchase up to two seats per transaction.");
                }
                request.setTicketId(seatTicket.getTicketId());

                BigDecimal price = seatTicket.getEffectivePrice();
                Timestamp saleTimestamp = request.getSaleTimestamp() != null
                        ? request.getSaleTimestamp()
                        : Timestamp.valueOf(LocalDateTime.now());
                ensureSaleBeforeEventStart(event, match, saleTimestamp);

                int saleId = insertSeatAndTicket(conn, request, customerId, request.getQuantity(), price, saleTimestamp);
                updateSeatStatus(conn, seat.getSeatId(), "Sold");

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);

                return new TicketPurchaseResult(
                        saleId,
                        customerId,
                        seat,
                        event,
                        match,
                        request.getQuantity(),
                        price,
                        price.multiply(BigDecimal.valueOf(request.getQuantity())),
                        saleTimestamp
                );
            } catch (Exception ex) {
                conn.rollback();
                throw ex instanceof SQLException ? (SQLException) ex : new SQLException(ex.getMessage(), ex);
            }
        }
    }

    public TicketRefundResult refundTicket(int saleRecordId, String reason, String processedBy) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // Fetch the sale record
                SeatAndTicket saleRecord = fetchSaleRecord(conn, saleRecordId, true);
                if (saleRecord == null) {
                    throw new IllegalArgumentException("Sale record not found.");
                }
                if (!saleRecord.isSold()) {
                    throw new IllegalStateException("This ticket has already been refunded.");
                }

                // Calculate refund amount
                BigDecimal refundAmount = saleRecord.getTotalPrice();
                Timestamp refundTimestamp = Timestamp.valueOf(LocalDateTime.now());

                // Update seat_and_ticket status to 'Refunded'
                updateSaleStatus(conn, saleRecordId, "Refunded");

                // Update seat status back to 'Available'
                updateSeatStatus(conn, saleRecord.getSeatId(), "Available");

                // Insert refund audit record
                insertRefundAudit(conn, saleRecordId, refundAmount, reason, processedBy);

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);

                return new TicketRefundResult(
                        saleRecordId,
                        saleRecord.getCustomerId(),
                        saleRecord.getSeatId(),
                        saleRecord.getEventId(),
                        saleRecord.getMatchId(),
                        refundTimestamp,
                        refundAmount,
                        reason,
                        processedBy
                );
            } catch (Exception ex) {
                conn.rollback();
                throw ex instanceof SQLException ? (SQLException) ex : new SQLException(ex.getMessage(), ex);
            }
        }
    }

    private SeatAndTicket fetchSaleRecord(Connection conn, int saleRecordId, boolean forUpdate) throws SQLException {
        String sql = "SELECT sat.seat_and_ticket_rec_id, sat.seat_id, sat.event_id, sat.customer_id, " +
                "sat.sale_datetime, sat.quantity, sat.unit_price, sat.total_price, sat.ticket_id, " +
                "sat.match_id, sat.sale_status, " +
                "c.customer_first_name, c.customer_last_name, " +
                "e.event_name, " +
                "s.seat_type " +
                "FROM seat_and_ticket sat " +
                "INNER JOIN customer c ON sat.customer_id = c.customer_id " +
                "INNER JOIN event e ON sat.event_id = e.event_id " +
                "INNER JOIN seat s ON sat.seat_id = s.seat_id " +
                "WHERE sat.seat_and_ticket_rec_id = ? " + (forUpdate ? "FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleRecordId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SeatAndTicket record = new SeatAndTicket();
                    record.setRecordId(rs.getInt("seat_and_ticket_rec_id"));
                    record.setSeatId(rs.getInt("seat_id"));
                    record.setEventId(rs.getInt("event_id"));
                    record.setCustomerId(rs.getInt("customer_id"));
                    record.setSaleDatetime(rs.getTimestamp("sale_datetime"));
                    record.setQuantity(rs.getInt("quantity"));
                    record.setUnitPrice(rs.getBigDecimal("unit_price"));
                    record.setTotalPrice(rs.getBigDecimal("total_price"));
                    record.setTicketId(rs.getInt("ticket_id"));
                    
                    Integer matchId = (Integer) rs.getObject("match_id");
                    record.setMatchId(matchId);
                    
                    record.setSaleStatus(rs.getString("sale_status"));
                    record.setCustomerName(rs.getString("customer_first_name") + " " + rs.getString("customer_last_name"));
                    record.setEventName(rs.getString("event_name"));
                    record.setSeatLabel(rs.getString("seat_type"));
                    
                    return record;
                }
            }
        }
        return null;
    }

    private void updateSaleStatus(Connection conn, int saleRecordId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE seat_and_ticket SET sale_status = ? WHERE seat_and_ticket_rec_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, saleRecordId);
            ps.executeUpdate();
        }
    }

    private void insertRefundAudit(Connection conn, int saleRecordId, BigDecimal refundAmount, 
                                   String reason, String processedBy) throws SQLException {
        String sql = "INSERT INTO ticket_refund_audit " +
                "(seat_and_ticket_rec_id, refund_amount, reason, processed_by) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleRecordId);
            ps.setBigDecimal(2, refundAmount);
            ps.setString(3, reason);
            ps.setString(4, processedBy);
            ps.executeUpdate();
        }
    }

    private Event fetchEvent(Connection conn, int eventId, boolean forUpdate) throws SQLException {
        String sql = "SELECT event_id, event_name, sport, match_date, event_time_start, event_time_end, " +
                "venue_address, venue_capacity, event_status FROM event WHERE event_id = ? " + (forUpdate ? "FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Event(
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getString("sport"),
                            rs.getDate("match_date"),
                            rs.getTime("event_time_start"),
                            rs.getTime("event_time_end"),
                            rs.getString("venue_address"),
                            rs.getString("event_status"),
                            rs.getInt("venue_capacity")
                    );
                }
            }
        }
        return null;
    }

    private Match fetchMatch(Connection conn, int matchId, boolean forUpdate) throws SQLException {
        String sql = "SELECT m.match_id, m.event_id, e.event_name, m.match_type, " +
t                "m.match_date, m.match_time_start, m.match_time_end, m.status, m.score_summary " +
                "FROM `match` m INNER JOIN event e ON m.event_id = e.event_id " +
                "WHERE m.match_id = ? " + (forUpdate ? "FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Match(
                            rs.getInt("match_id"),
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getString("match_type"),
                            rs.getDate("match_date"),
                            rs.getTime("match_time_start"),
                            rs.getTime("match_time_end"),
                            rs.getString("status"),
                            rs.getString("score_summary")
                    );
                }
            }
        }
        return null;
    }

    private Seat fetchSeat(Connection conn, int seatId, boolean forUpdate) throws SQLException {
        String sql = "SELECT s.seat_id, s.seat_type, s.venue_address, s.seat_status, " +
                "t.ticket_id, t.default_price, t.price, t.ticket_status " +
                "FROM seat s INNER JOIN ticket t ON s.ticket_id = t.ticket_id " +
                "WHERE s.seat_id = ? " + (forUpdate ? "FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ticket ticket = new Ticket(
                            rs.getInt("ticket_id"),
                            rs.getBigDecimal("default_price"),
                            rs.getBigDecimal("price"),
                            rs.getString("ticket_status")
                    );
                    return new Seat(
                            rs.getInt("seat_id"),
                            rs.getString("seat_type"),
                            rs.getString("venue_address"),
                            rs.getString("seat_status"),
                            ticket
                    );
                }
            }
        }
        throw new IllegalArgumentException("Seat not found.");
    }

    private void ensureSaleBeforeEventStart(Event event, Match match, Timestamp saleTimestamp) {
        if (event == null || saleTimestamp == null) {
            return;
        }
        LocalDateTime cutoff = LocalDateTime.of(
                event.getMatchDate().toLocalDate(),
                event.getEventTimeStart().toLocalTime()
        );
        if (match != null) {
            cutoff = LocalDateTime.of(
                    match.getMatchDate().toLocalDate(),
                    match.getMatchTimeStart().toLocalTime()
            );
        }
        if (saleTimestamp.toLocalDateTime().isAfter(cutoff)) {
            throw new IllegalStateException("Tickets can no longer be sold after the start time.");
        }
    }

    private void ensureSeatAvailable(Connection conn, int eventId, int seatId) throws SQLException {
        String sql = "SELECT 1 FROM seat_and_ticket WHERE event_id = ? AND seat_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, seatId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalStateException("Seat is already sold for this event.");
                }
            }
        }
    }

    private int resolveCustomer(Connection conn, TicketPurchaseRequest request) throws SQLException {
        if (request.getExistingCustomerId() != null) {
            return request.getExistingCustomerId();
        }
        ensureContactDetailsPresent(request.getEmail(), request.getPhone());

        String sql = "INSERT INTO customer " +
                "(customer_first_name, customer_last_name, phone_number, email, organization, " +
                "registration_date, preferred_team, customer_status, payment_method) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.getFirstName());
            ps.setString(2, request.getLastName());
            ps.setString(3, request.getPhone());
            ps.setString(4, request.getEmail());
            ps.setString(5, request.getOrganization());
            ps.setDate(6, request.getRegistrationDate() != null
                    ? request.getRegistrationDate()
                    : java.sql.Date.valueOf(LocalDate.now()));
            ps.setString(7, request.getPreferredTeam());
            ps.setString(8, request.getCustomerStatus());
            ps.setString(9, request.getPaymentMethod());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to create customer record.");
    }

    private int insertSeatAndTicket(Connection conn,
                                    TicketPurchaseRequest request,
                                    int customerId,
                                    int quantity,
                                    BigDecimal unitPrice,
                                    Timestamp saleTimestamp) throws SQLException {
        String sql = "INSERT INTO seat_and_ticket " +
                "(seat_id, event_id, customer_id, sale_datetime, quantity, unit_price, ticket_id, match_id, sale_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Sold')";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, request.getSeatId());
            ps.setInt(2, request.getEventId());
            ps.setInt(3, customerId);
            ps.setTimestamp(4, saleTimestamp);
            ps.setInt(5, quantity);
            ps.setBigDecimal(6, unitPrice);
            ps.setInt(7, request.getTicketId());

            if (request.getMatchId() != null) {
                ps.setInt(8, request.getMatchId());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to create ticket sale record.");
    }

    private void updateSeatStatus(Connection conn, int seatId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE seat SET seat_status = ? WHERE seat_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, seatId);
            ps.executeUpdate();
        }
    }

    private void ensureContactDetailsPresent(String email, String phone) {
        boolean hasPhone = phone != null && !phone.trim().isEmpty();
        boolean hasEmail = email != null && !email.trim().isEmpty();
        if (!hasPhone && !hasEmail) {
            throw new IllegalArgumentException("Provide at least one contact detail (phone or email) for the customer.");
        }
    }

    private void validateEventForSale(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        if ("Cancelled".equalsIgnoreCase(event.getEventStatus())) {
            throw new IllegalStateException("Cannot sell tickets for a cancelled event.");
        }

        LocalDate eventDate = event.getMatchDate().toLocalDate();
        LocalTime startTime = event.getEventTimeStart().toLocalTime();
        LocalDateTime eventStart = LocalDateTime.of(eventDate, startTime);

        if (LocalDateTime.now().isAfter(eventStart)) {
            throw new IllegalStateException("Event has already started or finished; ticketing is closed.");
        }
    }

    private void validateMatchForSale(Match match) {
        if (match == null) {
            throw new IllegalArgumentException("Match not found.");
        }
        if (!"Scheduled".equalsIgnoreCase(match.getStatus())) {
            throw new IllegalStateException("Match is not in a scheduled state for ticket sales.");
        }
    }

    public static class TicketPurchaseRequest {
        private Integer existingCustomerId;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private String organization;
        private java.sql.Date registrationDate;
        private String preferredTeam;
        private String customerStatus;
        private String paymentMethod;

        private int eventId;
        private Integer matchId;
        private int seatId;
        private int ticketId;
        private int quantity = 1;
        private Timestamp saleTimestamp;

        public Integer getExistingCustomerId() {
            return existingCustomerId;
        }

        public void setExistingCustomerId(Integer existingCustomerId) {
            this.existingCustomerId = existingCustomerId;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public java.sql.Date getRegistrationDate() {
            return registrationDate;
        }

        public void setRegistrationDate(java.sql.Date registrationDate) {
            this.registrationDate = registrationDate;
        }

        public String getPreferredTeam() {
            return preferredTeam;
        }

        public void setPreferredTeam(String preferredTeam) {
            this.preferredTeam = preferredTeam;
        }

        public String getCustomerStatus() {
            return customerStatus;
        }

        public void setCustomerStatus(String customerStatus) {
            this.customerStatus = customerStatus;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public int getEventId() {
            return eventId;
        }

        public void setEventId(int eventId) {
            this.eventId = eventId;
        }

        public Integer getMatchId() {
            return matchId;
        }

        public void setMatchId(Integer matchId) {
            this.matchId = matchId;
        }

        public int getSeatId() {
            return seatId;
        }

        public void setSeatId(int seatId) {
            this.seatId = seatId;
        }

        public int getTicketId() {
            return ticketId;
        }

        public void setTicketId(int ticketId) {
            this.ticketId = ticketId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public Timestamp getSaleTimestamp() {
            return saleTimestamp;
        }

        public void setSaleTimestamp(Timestamp saleTimestamp) {
            this.saleTimestamp = saleTimestamp;
        }
    }

    public record TicketPurchaseResult(int saleRecordId,
                                       int customerId,
                                       Seat seat,
                                       Event event,
                                       Match match,
                                       int quantity,
                                       BigDecimal unitPrice,
                                       BigDecimal totalAmount,
                                       Timestamp saleTimestamp) {}

    public record TicketRefundResult(int saleRecordId,
                                     int customerId,
                                     int seatId,
                                     int eventId,
                                     Integer matchId,
                                     Timestamp refundTimestamp,
                                     BigDecimal amountRefunded,
                                     String reason,
                                     String processedBy) {}
}
