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

                int customerId = resolveCustomer(conn, request);

                BigDecimal price = resolvePrice(conn, request.getTicketId(), request.getPriceOverride());
                Timestamp saleTimestamp = request.getSaleTimestamp() != null
                        ? request.getSaleTimestamp()
                        : Timestamp.valueOf(LocalDateTime.now());

                int saleId = insertSeatAndTicket(conn, request, customerId, price, saleTimestamp);
                updateSeatStatus(conn, seat.getSeatId(), "Sold");

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);

                return new TicketPurchaseResult(
                        saleId,
                        customerId,
                        seat,
                        event,
                        match,
                        price,
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
                SaleSnapshot sale = fetchSaleSnapshot(conn, saleRecordId, true);
                if (sale == null) {
                    throw new IllegalArgumentException("Ticket sale record not found.");
                }
                if (!"Sold".equalsIgnoreCase(sale.saleStatus())) {
                    throw new IllegalStateException("Only tickets with status 'Sold' can be refunded.");
                }

                Event event = fetchEvent(conn, sale.eventId(), false);
                if (event == null) {
                    throw new IllegalStateException("Event linked to the sale no longer exists.");
                }
                ensureRefundAllowed(event);

                Timestamp refundTimestamp = Timestamp.valueOf(LocalDateTime.now());

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE seat_and_ticket SET sale_status = 'Refunded', refund_datetime = ? WHERE seat_and_ticket_rec_id = ?")) {
                    ps.setTimestamp(1, refundTimestamp);
                    ps.setInt(2, saleRecordId);
                    ps.executeUpdate();
                }

                updateSeatStatus(conn, sale.seatId(), "Available");
                insertRefundAudit(conn, saleRecordId, sale.priceSold(), refundTimestamp, reason, processedBy);

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);

                return new TicketRefundResult(
                        saleRecordId,
                        sale.customerId(),
                        sale.seatId(),
                        sale.eventId(),
                        sale.matchId(),
                        refundTimestamp,
                        sale.priceSold(),
                        reason,
                        processedBy
                );
            } catch (Exception ex) {
                conn.rollback();
                throw ex instanceof SQLException ? (SQLException) ex : new SQLException(ex.getMessage(), ex);
            }
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
                "m.match_date, m.match_time_start, m.match_time_end, m.status, m.score_summary " +
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
        String sql = "SELECT seat_id, seat_type, seat_section, venue_address, seat_status " +
                "FROM seat WHERE seat_id = ? " + (forUpdate ? "FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Seat(
                            rs.getInt("seat_id"),
                            rs.getString("seat_type"),
                            rs.getString("seat_section"),
                            rs.getString("venue_address"),
                            rs.getString("seat_status")
                    );
                }
            }
        }
        throw new IllegalArgumentException("Seat not found.");
    }

    private void ensureSeatAvailable(Connection conn, int eventId, int seatId) throws SQLException {
        String sql = "SELECT 1 FROM seat_and_ticket WHERE event_id = ? AND seat_id = ? AND sale_status = 'Sold'";

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

        String sql = "INSERT INTO customer " +
                "(customer_first_name, customer_last_name, phone_number, email, organization, " +
                "registration_date, preferred_sport, customer_status, payment_method) " +
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
            ps.setString(7, request.getPreferredSport());
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

    private BigDecimal resolvePrice(Connection conn, int ticketId, BigDecimal override) throws SQLException {
        if (override != null) {
            return override;
        }

        String sql = "SELECT COALESCE(price, default_price) AS effective_price FROM ticket WHERE ticket_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("effective_price");
                }
            }
        }

        throw new IllegalArgumentException("Ticket template not found.");
    }

    private int insertSeatAndTicket(Connection conn,
                                    TicketPurchaseRequest request,
                                    int customerId,
                                    BigDecimal price,
                                    Timestamp saleTimestamp) throws SQLException {
        String sql = "INSERT INTO seat_and_ticket " +
                "(seat_id, event_id, customer_id, sale_datetime, price_sold, ticket_id, match_id, sale_status, refund_datetime) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'Sold', NULL)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, request.getSeatId());
            ps.setInt(2, request.getEventId());
            ps.setInt(3, customerId);
            ps.setTimestamp(4, saleTimestamp);
            ps.setBigDecimal(5, price);
            ps.setInt(6, request.getTicketId());

            if (request.getMatchId() != null) {
                ps.setInt(7, request.getMatchId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
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

    private SaleSnapshot fetchSaleSnapshot(Connection conn, int saleRecordId, boolean forUpdate) throws SQLException {
        String sql = "SELECT seat_and_ticket_rec_id, seat_id, event_id, customer_id, match_id, " +
                "sale_status, price_sold FROM seat_and_ticket WHERE seat_and_ticket_rec_id = ? " +
                (forUpdate ? "FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleRecordId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SaleSnapshot(
                            rs.getInt("seat_and_ticket_rec_id"),
                            rs.getInt("seat_id"),
                            rs.getInt("event_id"),
                            rs.getInt("customer_id"),
                            (Integer) rs.getObject("match_id"),
                            rs.getString("sale_status"),
                            rs.getBigDecimal("price_sold")
                    );
                }
            }
        }
        return null;
    }

    private void insertRefundAudit(Connection conn,
                                   int saleId,
                                   BigDecimal amount,
                                   Timestamp refundTimestamp,
                                   String reason,
                                   String processedBy) throws SQLException {
        String sql = "INSERT INTO ticket_refund_audit " +
                "(seat_and_ticket_rec_id, refund_datetime, refund_amount, reason, processed_by) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            ps.setTimestamp(2, refundTimestamp);
            ps.setBigDecimal(3, amount);
            ps.setString(4, reason);
            ps.setString(5, processedBy);
            ps.executeUpdate();
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

    private void ensureRefundAllowed(Event event) {
        LocalDateTime eventStart = LocalDateTime.of(
                event.getMatchDate().toLocalDate(),
                event.getEventTimeStart().toLocalTime()
        );

        if (LocalDateTime.now().isAfter(eventStart)) {
            throw new IllegalStateException("Event already started. Refunds are no longer allowed.");
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
        private String preferredSport;
        private String customerStatus;
        private String paymentMethod;

        private int eventId;
        private Integer matchId;
        private int seatId;
        private int ticketId;
        private BigDecimal priceOverride;
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

        public String getPreferredSport() {
            return preferredSport;
        }

        public void setPreferredSport(String preferredSport) {
            this.preferredSport = preferredSport;
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

        public BigDecimal getPriceOverride() {
            return priceOverride;
        }

        public void setPriceOverride(BigDecimal priceOverride) {
            this.priceOverride = priceOverride;
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
                                       BigDecimal pricePaid,
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

    private record SaleSnapshot(int saleId,
                                int seatId,
                                int eventId,
                                int customerId,
                                Integer matchId,
                                String saleStatus,
                                BigDecimal priceSold) {}
}
