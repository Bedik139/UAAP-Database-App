import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SeatAndTicketDAO {

    public void insertRecord(SeatAndTicket record) throws SQLException {
        String sql = "INSERT INTO seat_and_ticket " +
                "(seat_id, event_id, customer_id, sale_datetime, price_sold, ticket_id, match_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

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
        List<SeatAndTicket> list = new ArrayList<>();

        String sql = "SELECT sat.seat_and_ticket_rec_id, sat.seat_id, sat.event_id, sat.customer_id, " +
                "sat.sale_datetime, sat.price_sold, sat.ticket_id, sat.match_id, " +
                "CONCAT('#', s.seat_id, ' ', s.seat_type) AS seat_label, " +
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
                "LEFT JOIN event ev ON m.event_id = ev.event_id " +
                "ORDER BY sat.seat_and_ticket_rec_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SeatAndTicket record = new SeatAndTicket(
                        rs.getInt("seat_and_ticket_rec_id"),
                        rs.getInt("seat_id"),
                        rs.getInt("event_id"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("sale_datetime"),
                        rs.getBigDecimal("price_sold"),
                        rs.getInt("ticket_id"),
                        (Integer) rs.getObject("match_id"),
                        rs.getString("seat_label"),
                        rs.getString("event_name"),
                        rs.getString("customer_name"),
                        rs.getString("ticket_label"),
                        rs.getString("match_label")
                );
                list.add(record);
            }
        }

        return list;
    }

    public void updateRecord(SeatAndTicket record) throws SQLException {
        String sql = "UPDATE seat_and_ticket SET seat_id = ?, event_id = ?, customer_id = ?, " +
                "sale_datetime = ?, price_sold = ?, ticket_id = ?, match_id = ? " +
                "WHERE seat_and_ticket_rec_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindRecord(ps, record);
            ps.setInt(8, record.getRecordId());
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
    }
}
