import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    // CREATE
    public void insertEvent(Event ev) throws SQLException {
        String sql = "INSERT INTO event " +
                "(event_name, sport, match_date, event_time_start, event_time_end, venue_address) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ev.getEventName());
            ps.setString(2, ev.getSport());
            ps.setDate(3, ev.getMatchDate());
            ps.setTime(4, ev.getEventTimeStart());
            ps.setTime(5, ev.getEventTimeEnd());
            ps.setString(6, ev.getVenueAddress());

            ps.executeUpdate();
        }
    }

    // READ (all rows)
    public List<Event> getAllEvents() throws SQLException {
        List<Event> list = new ArrayList<>();

        String sql = "SELECT event_id, event_name, sport, match_date, " +
                "event_time_start, event_time_end, venue_address " +
                "FROM event ORDER BY event_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Event ev = new Event();
                ev.setEventId(rs.getInt("event_id"));
                ev.setEventName(rs.getString("event_name"));
                ev.setSport(rs.getString("sport"));
                ev.setMatchDate(rs.getDate("match_date"));
                ev.setEventTimeStart(rs.getTime("event_time_start"));
                ev.setEventTimeEnd(rs.getTime("event_time_end"));
                ev.setVenueAddress(rs.getString("venue_address"));
                list.add(ev);
            }
        }

        return list;
    }

    // UPDATE
    public void updateEvent(Event ev) throws SQLException {
        String sql = "UPDATE event SET " +
                "event_name = ?, sport = ?, match_date = ?, " +
                "event_time_start = ?, event_time_end = ?, venue_address = ? " +
                "WHERE event_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ev.getEventName());
            ps.setString(2, ev.getSport());
            ps.setDate(3, ev.getMatchDate());
            ps.setTime(4, ev.getEventTimeStart());
            ps.setTime(5, ev.getEventTimeEnd());
            ps.setString(6, ev.getVenueAddress());
            ps.setInt(7, ev.getEventId());

            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteEvent(int eventId) throws SQLException {
        String sql = "DELETE FROM event WHERE event_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ps.executeUpdate();
        }
    }
}
