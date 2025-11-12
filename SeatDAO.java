import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {

    private static final String BASE_SELECT =
            "SELECT s.seat_id, s.seat_type, s.venue_address, s.seat_status, s.ticket_id, " +
            "t.ticket_id AS linked_ticket_id, t.default_price, t.price, t.ticket_status " +
            "FROM seat s INNER JOIN ticket t ON s.ticket_id = t.ticket_id ";

    public List<Seat> getAllSeats() throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY seat_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                seats.add(mapSeat(rs));
            }
        }

        return seats;
    }

    public List<Seat> getAvailableSeatsForEvent(int eventId, String venueAddress) throws SQLException {
        List<Seat> seats = new ArrayList<>();

        String sql = BASE_SELECT +
                "WHERE s.seat_status = 'Available' " +
                "AND s.seat_type IN ('Upper Box','Lower Box') " +
                "AND seat_id NOT IN ( " +
                "   SELECT seat_id FROM seat_and_ticket " +
                "   WHERE event_id = ? AND sale_status = 'Sold'" +
                ") ";
        if (venueAddress != null && !venueAddress.isEmpty()) {
            sql += " AND s.venue_address = ?";
        }
        sql += " ORDER BY s.seat_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            if (venueAddress != null && !venueAddress.isEmpty()) {
                ps.setString(2, venueAddress);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapSeat(rs));
                }
            }
        }

        return seats;
    }

    public Seat getSeatById(int seatId) throws SQLException {
        String sql = BASE_SELECT + "WHERE s.seat_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, seatId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSeat(rs);
                }
            }
        }

        return null;
    }

    public void updateSeatStatus(int seatId, String status) throws SQLException {
        String sql = "UPDATE seat SET seat_status = ? WHERE seat_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, seatId);

            ps.executeUpdate();
        }
    }

    private Seat mapSeat(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket(
                rs.getInt("linked_ticket_id"),
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
