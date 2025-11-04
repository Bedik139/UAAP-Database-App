import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {

    private static final String BASE_SELECT =
            "SELECT seat_id, seat_type, seat_section, venue_address, seat_status FROM seat ";

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

    public List<Seat> getAvailableSeatsForEvent(int eventId) throws SQLException {
        List<Seat> seats = new ArrayList<>();

        String sql = BASE_SELECT +
                "WHERE seat_status = 'Available' " +
                "AND seat_id NOT IN ( " +
                "   SELECT seat_id FROM seat_and_ticket " +
                "   WHERE event_id = ? AND sale_status = 'Sold'" +
                ") ORDER BY seat_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapSeat(rs));
                }
            }
        }

        return seats;
    }

    public Seat getSeatById(int seatId) throws SQLException {
        String sql = BASE_SELECT + "WHERE seat_id = ?";

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
        return new Seat(
                rs.getInt("seat_id"),
                rs.getString("seat_type"),
                rs.getString("seat_section"),
                rs.getString("venue_address"),
                rs.getString("seat_status")
        );
    }
}
