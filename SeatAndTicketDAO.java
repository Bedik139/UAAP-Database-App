import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SeatAndTicketDAO {

    private static final String BASE_SELECT =
            "SELECT sat.seat_and_ticket_rec_id, sat.seat_id, sat.event_id, sat.customer_id, " +
            "sat.sale_datetime, sat.price_sold, sat.ticket_id, sat.match_id, sat.sale_status, sat.refund_datetime, " +
            "CONCAT('#', s.seat_id, ' ', s.seat_type, ' ', COALESCE(s.seat_section, '')) AS seat_label, " +
            "e.event_name, CONCAT(c.customer_first_name, ' ', c.customer_last_name) AS customer_name, " +
            "CONCAT('#', t.ticket_id, ' ', COALESCE(t.price, t.default_price)) AS ticket_label, " +
            "CASE WHEN sat.match_id IS NULL THEN NULL " +
            "ELSE CONCAT('#', m.match_id, ' ', ev.event_name, ' (', m.match_type, ')') END AS match_label " +
            "FROM seat_and_ticket sat " +
            "INNER JOIN seat s ON sat.seat_id = s.seat_id " +
            "INNER JOIN event e ON sat.event_id = e.event_id " +
            "INNER JOIN customer c ON sat.customer_id = c.customer_id " +
            "INNER JOIN ticket t ON sat.ticket_id = t.ticket_id " +
            "LEFT JOIN `match` m ON sat.match_id = m.match_id " +
            "LEFT JOIN event ev ON m.event_id = ev.event_id ";

    public void insertRecord(SeatAndTicket record) throws SQLException {
        String sql = "INSERT INTO seat_and_ticket " +
                "(seat_id, event_id, customer_id, sale_datetime, price_sold, ticket_id, match_id, sale_status, refund_datetime) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindRecord(ps, record);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setRecordId(rs.getInt(1));
                }
            }
        }
    }

    public List<SeatAndTicket> getAllRecords() throws SQLException {
        List<SeatAndTicket> records = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY sat.seat_and_ticket_rec_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                records.add(mapRow(rs));
            }
        }

        return records;
    }

    public SeatAndTicket getRecordById(int recordId) throws SQLException {
        String sql = BASE_SELECT + "WHERE sat.seat_and_ticket_rec_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    public SeatAndTicket getActiveSaleForSeat(int eventId, int seatId) throws SQLException {
        String sql = BASE_SELECT +
                "WHERE sat.event_id = ? AND sat.seat_id = ? AND sat.sale_status = 'Sold'";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ps.setInt(2, seatId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    public void updateRecord(SeatAndTicket record) throws SQLException {
        String sql = "UPDATE seat_and_ticket SET seat_id = ?, event_id = ?, customer_id = ?, " +
                "sale_datetime = ?, price_sold = ?, ticket_id = ?, match_id = ?, sale_status = ?, refund_datetime = ? " +
                "WHERE seat_and_ticket_rec_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindRecord(ps, record);
            ps.setInt(10, record.getRecordId());
            ps.executeUpdate();
        }
    }

    public void updateSaleStatus(int recordId, String saleStatus, Timestamp refundTimestamp) throws SQLException {
        String sql = "UPDATE seat_and_ticket SET sale_status = ?, refund_datetime = ? WHERE seat_and_ticket_rec_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, saleStatus);
            if (refundTimestamp != null) {
                ps.setTimestamp(2, refundTimestamp);
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setInt(3, recordId);

            ps.executeUpdate();
        }
    }

    public void deleteRecord(int recordId) throws SQLException {
        String sql = "DELETE FROM seat_and_ticket WHERE seat_and_ticket_rec_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordId);
            ps.executeUpdate();
        }
    }

    private void bindRecord(PreparedStatement ps, SeatAndTicket record) throws SQLException {
        ps.setInt(1, record.getSeatId());
        ps.setInt(2, record.getEventId());
        ps.setInt(3, record.getCustomerId());
        ps.setTimestamp(4, record.getSaleDatetime());
        ps.setBigDecimal(5, record.getPriceSold());
        ps.setInt(6, record.getTicketId());

        if (record.getMatchId() != null) {
            ps.setInt(7, record.getMatchId());
        } else {
            ps.setNull(7, Types.INTEGER);
        }

        ps.setString(8, record.getSaleStatus());

        if (record.getRefundDatetime() != null) {
            ps.setTimestamp(9, record.getRefundDatetime());
        } else {
            ps.setNull(9, Types.TIMESTAMP);
        }
    }

    private SeatAndTicket mapRow(ResultSet rs) throws SQLException {
        SeatAndTicket record = new SeatAndTicket();
        record.setRecordId(rs.getInt("seat_and_ticket_rec_id"));
        record.setSeatId(rs.getInt("seat_id"));
        record.setEventId(rs.getInt("event_id"));
        record.setCustomerId(rs.getInt("customer_id"));
        record.setSaleDatetime(rs.getTimestamp("sale_datetime"));
        record.setPriceSold(rs.getBigDecimal("price_sold"));
        record.setTicketId(rs.getInt("ticket_id"));
        record.setMatchId((Integer) rs.getObject("match_id"));
        record.setSaleStatus(rs.getString("sale_status"));
        record.setRefundDatetime(rs.getTimestamp("refund_datetime"));
        record.setSeatLabel(rs.getString("seat_label"));
        record.setEventName(rs.getString("event_name"));
        record.setCustomerName(rs.getString("customer_name"));
        record.setTicketLabel(rs.getString("ticket_label"));
        record.setMatchLabel(rs.getString("match_label"));
        return record;
    }
}
