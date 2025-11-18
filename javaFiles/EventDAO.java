import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    private static final String BASE_SELECT =
            "SELECT event_id, event_name, sport, event_date, event_time_start, " +
            "event_time_end, venue_address, venue_capacity, event_status FROM event ";

    public void insertEvent(Event event) throws SQLException {
        String sql = "INSERT INTO event " +
                "(event_name, sport, event_date, event_time_start, event_time_end, venue_address, venue_capacity, event_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, event.getEventName());
            ps.setString(2, event.getSport());
            ps.setDate(3, event.getEventDate());
            ps.setTime(4, event.getEventTimeStart());
            ps.setTime(5, event.getEventTimeEnd());
            ps.setString(6, event.getVenueAddress());
            
            // Auto-set capacity based on venue
            int capacity = getVenueCapacity(event.getVenueAddress());
            ps.setInt(7, capacity);
            event.setVenueCapacity(capacity); // Update the event object too
            
            ps.setString(8, event.getEventStatus());

            ps.executeUpdate();
        }
    }

    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY CASE WHEN event_status = 'Completed' THEN 1 ELSE 0 END ASC, event_id ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                events.add(mapRow(rs));
            }
        }

        return events;
    }

    public Event getEventById(int eventId) throws SQLException {
        String sql = BASE_SELECT + "WHERE event_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    public void updateEvent(Event event) throws SQLException {
        String sql = "UPDATE event SET " +
                "event_name = ?, sport = ?, event_date = ?, event_time_start = ?, " +
                "event_time_end = ?, venue_address = ?, venue_capacity = ?, event_status = ? " +
                "WHERE event_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, event.getEventName());
            ps.setString(2, event.getSport());
            ps.setDate(3, event.getEventDate());
            ps.setTime(4, event.getEventTimeStart());
            ps.setTime(5, event.getEventTimeEnd());
            ps.setString(6, event.getVenueAddress());
            
            // Auto-set capacity based on venue
            int capacity = getVenueCapacity(event.getVenueAddress());
            ps.setInt(7, capacity);
            event.setVenueCapacity(capacity); // Update the event object too
            
            ps.setString(8, event.getEventStatus());
            ps.setInt(9, event.getEventId());

            ps.executeUpdate();
        }
    }

    public void updateEventStatus(int eventId, String status) throws SQLException {
        String sql = "UPDATE event SET event_status = ? WHERE event_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, eventId);

            ps.executeUpdate();
        }
    }

    public void deleteEvent(int eventId) throws SQLException {
        String sql = "DELETE FROM event WHERE event_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ps.executeUpdate();
        }
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getInt("event_id"));
        event.setEventName(rs.getString("event_name"));
        event.setSport(rs.getString("sport"));
        event.setEventDate(rs.getDate("event_date"));
        event.setEventTimeStart(rs.getTime("event_time_start"));
        event.setEventTimeEnd(rs.getTime("event_time_end"));
        event.setVenueAddress(rs.getString("venue_address"));
        event.setEventStatus(rs.getString("event_status"));
        event.setVenueCapacity(rs.getInt("venue_capacity"));
        return event;
    }

    /**
     * Helper method to get the capacity for a venue address.
     * Uses the Venue enum to get the standard capacity.
     */
    private int getVenueCapacity(String venueAddress) {
        Venue venue = Venue.fromName(venueAddress);
        if (venue != null) {
            return venue.getCapacity();
        }
        // Default fallback if venue not found
        return 10000;
    }
}
