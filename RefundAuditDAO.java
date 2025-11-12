import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RefundAuditDAO {

    public List<RefundAudit> getAllRefunds() throws SQLException {
        List<RefundAudit> refunds = new ArrayList<>();
        String sql = "SELECT audit_id, seat_and_ticket_rec_id, refund_amount, refund_datetime, reason, processed_by " +
                     "FROM ticket_refund_audit ORDER BY refund_datetime DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                RefundAudit refund = new RefundAudit(
                    rs.getInt("audit_id"),
                    rs.getInt("seat_and_ticket_rec_id"),
                    rs.getBigDecimal("refund_amount"),
                    rs.getTimestamp("refund_datetime"),
                    rs.getString("reason"),
                    rs.getString("processed_by")
                );
                refunds.add(refund);
            }
        }
        return refunds;
    }

    public List<RefundAuditDetail> getAllRefundsWithDetails() throws SQLException {
        List<RefundAuditDetail> refunds = new ArrayList<>();
        String sql = "SELECT " +
                     "  tra.audit_id, " +
                     "  tra.seat_and_ticket_rec_id, " +
                     "  tra.refund_amount, " +
                     "  tra.refund_datetime, " +
                     "  tra.reason, " +
                     "  tra.processed_by, " +
                     "  c.customer_first_name, " +
                     "  c.customer_last_name, " +
                     "  c.email, " +
                     "  e.event_name, " +
                     "  s.seat_type, " +
                     "  sat.sale_datetime " +
                     "FROM ticket_refund_audit tra " +
                     "JOIN seat_and_ticket sat ON tra.seat_and_ticket_rec_id = sat.seat_and_ticket_rec_id " +
                     "JOIN customer c ON sat.customer_id = c.customer_id " +
                     "JOIN event e ON sat.event_id = e.event_id " +
                     "JOIN seat s ON sat.seat_id = s.seat_id " +
                     "ORDER BY tra.refund_datetime DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                RefundAuditDetail detail = new RefundAuditDetail(
                    rs.getInt("audit_id"),
                    rs.getInt("seat_and_ticket_rec_id"),
                    rs.getBigDecimal("refund_amount"),
                    rs.getTimestamp("refund_datetime"),
                    rs.getString("reason"),
                    rs.getString("processed_by"),
                    rs.getString("customer_first_name") + " " + rs.getString("customer_last_name"),
                    rs.getString("email"),
                    rs.getString("event_name"),
                    rs.getString("seat_type"),
                    rs.getTimestamp("sale_datetime")
                );
                refunds.add(detail);
            }
        }
        return refunds;
    }

    public void insertRefund(RefundAudit refund) throws SQLException {
        String sql = "INSERT INTO ticket_refund_audit (seat_and_ticket_rec_id, refund_amount, refund_datetime, reason, processed_by) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, refund.getSeatAndTicketRecId());
            stmt.setBigDecimal(2, refund.getRefundAmount());
            stmt.setTimestamp(3, refund.getRefundDatetime());
            stmt.setString(4, refund.getReason());
            stmt.setString(5, refund.getProcessedBy());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    refund.setAuditId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public RefundAudit getRefundByAuditId(int auditId) throws SQLException {
        String sql = "SELECT audit_id, seat_and_ticket_rec_id, refund_amount, refund_datetime, reason, processed_by " +
                     "FROM ticket_refund_audit WHERE audit_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, auditId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new RefundAudit(
                        rs.getInt("audit_id"),
                        rs.getInt("seat_and_ticket_rec_id"),
                        rs.getBigDecimal("refund_amount"),
                        rs.getTimestamp("refund_datetime"),
                        rs.getString("reason"),
                        rs.getString("processed_by")
                    );
                }
            }
        }
        return null;
    }

    public List<RefundAuditDetail> searchRefunds(String searchQuery, Date fromDate, Date toDate) throws SQLException {
        List<RefundAuditDetail> refunds = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "  tra.audit_id, " +
            "  tra.seat_and_ticket_rec_id, " +
            "  tra.refund_amount, " +
            "  tra.refund_datetime, " +
            "  tra.reason, " +
            "  tra.processed_by, " +
            "  c.customer_first_name, " +
            "  c.customer_last_name, " +
            "  c.email, " +
            "  e.event_name, " +
            "  s.seat_type, " +
            "  sat.sale_datetime " +
            "FROM ticket_refund_audit tra " +
            "JOIN seat_and_ticket sat ON tra.seat_and_ticket_rec_id = sat.seat_and_ticket_rec_id " +
            "JOIN customer c ON sat.customer_id = c.customer_id " +
            "JOIN event e ON sat.event_id = e.event_id " +
            "JOIN seat s ON sat.seat_id = s.seat_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (c.customer_first_name LIKE ? OR c.customer_last_name LIKE ? OR c.email LIKE ? OR e.event_name LIKE ?) ");
            String likeQuery = "%" + searchQuery + "%";
            params.add(likeQuery);
            params.add(likeQuery);
            params.add(likeQuery);
            params.add(likeQuery);
        }

        if (fromDate != null) {
            sql.append("AND DATE(tra.refund_datetime) >= ? ");
            params.add(fromDate);
        }

        if (toDate != null) {
            sql.append("AND DATE(tra.refund_datetime) <= ? ");
            params.add(toDate);
        }

        sql.append("ORDER BY tra.refund_datetime DESC");

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RefundAuditDetail detail = new RefundAuditDetail(
                        rs.getInt("audit_id"),
                        rs.getInt("seat_and_ticket_rec_id"),
                        rs.getBigDecimal("refund_amount"),
                        rs.getTimestamp("refund_datetime"),
                        rs.getString("reason"),
                        rs.getString("processed_by"),
                        rs.getString("customer_first_name") + " " + rs.getString("customer_last_name"),
                        rs.getString("email"),
                        rs.getString("event_name"),
                        rs.getString("seat_type"),
                        rs.getTimestamp("sale_datetime")
                    );
                    refunds.add(detail);
                }
            }
        }
        return refunds;
    }

    // Inner class for detailed refund information
    public static class RefundAuditDetail {
        private final int auditId;
        private final int seatAndTicketRecId;
        private final BigDecimal refundAmount;
        private final Timestamp refundDatetime;
        private final String reason;
        private final String processedBy;
        private final String customerName;
        private final String customerEmail;
        private final String eventName;
        private final String seatType;
        private final Timestamp saleDateTime;

        public RefundAuditDetail(int auditId, int seatAndTicketRecId, BigDecimal refundAmount,
                                 Timestamp refundDatetime, String reason, String processedBy,
                                 String customerName, String customerEmail, String eventName,
                                 String seatType, Timestamp saleDateTime) {
            this.auditId = auditId;
            this.seatAndTicketRecId = seatAndTicketRecId;
            this.refundAmount = refundAmount;
            this.refundDatetime = refundDatetime;
            this.reason = reason;
            this.processedBy = processedBy;
            this.customerName = customerName;
            this.customerEmail = customerEmail;
            this.eventName = eventName;
            this.seatType = seatType;
            this.saleDateTime = saleDateTime;
        }

        public int getAuditId() { return auditId; }
        public int getSeatAndTicketRecId() { return seatAndTicketRecId; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public Timestamp getRefundDatetime() { return refundDatetime; }
        public String getReason() { return reason; }
        public String getProcessedBy() { return processedBy; }
        public String getCustomerName() { return customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public String getEventName() { return eventName; }
        public String getSeatType() { return seatType; }
        public Timestamp getSaleDateTime() { return saleDateTime; }
    }
}
