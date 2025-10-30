import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {

    public List<Seat> getAllSeats() throws SQLException {
        List<Seat> seats = new ArrayList<>();

        String sql = "SELECT seat_id, seat_type FROM seat ORDER BY seat_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Seat seat = new Seat(
                        rs.getInt("seat_id"),
                        rs.getString("seat_type")
                );
                seats.add(seat);
            }
        }

        return seats;
    }
}
