import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EventPersonnelDAO {

    public void insertPersonnel(EventPersonnel personnel) throws SQLException {
        String sql = "INSERT INTO event_personnel " +
                "(personnel_first_name, personnel_last_name, availability_status, role, affiliation, contact_no, event_id, match_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindPersonnel(ps, personnel);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    personnel.setPersonnelId(rs.getInt(1));
                }
            }
        }
    }

    public List<EventPersonnel> getAllPersonnel() throws SQLException {
        List<EventPersonnel> list = new ArrayList<>();

        String sql = "SELECT ep.personnel_id, ep.personnel_first_name, ep.personnel_last_name, " +
                "ep.availability_status, ep.role, ep.affiliation, ep.contact_no, ep.event_id, ep.match_id, " +
                "e.event_name, " +
                "CASE WHEN ep.match_id IS NULL THEN NULL " +
                "ELSE CONCAT('#', m.match_id, ' ', ev.event_name, ' (', m.match_type, ')') END AS match_label " +
                "FROM event_personnel ep " +
                "INNER JOIN event e ON ep.event_id = e.event_id " +
                "LEFT JOIN `match` m ON ep.match_id = m.match_id " +
                "LEFT JOIN event ev ON m.event_id = ev.event_id " +
                "ORDER BY ep.personnel_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EventPersonnel personnel = new EventPersonnel(
                        rs.getInt("personnel_id"),
                        rs.getString("personnel_first_name"),
                        rs.getString("personnel_last_name"),
                        rs.getString("availability_status"),
                        rs.getString("role"),
                        rs.getString("affiliation"),
                        rs.getString("contact_no"),
                        rs.getInt("event_id"),
                        (Integer) rs.getObject("match_id"),
                        rs.getString("event_name"),
                        rs.getString("match_label")
                );
                list.add(personnel);
            }
        }

        return list;
    }

    public void updatePersonnel(EventPersonnel personnel) throws SQLException {
        String sql = "UPDATE event_personnel SET personnel_first_name = ?, personnel_last_name = ?, " +
                "availability_status = ?, role = ?, affiliation = ?, contact_no = ?, event_id = ?, match_id = ? " +
                "WHERE personnel_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindPersonnel(ps, personnel);
            ps.setInt(9, personnel.getPersonnelId());
            ps.executeUpdate();
        }
    }

    public void deletePersonnel(int personnelId) throws SQLException {
        String sql = "DELETE FROM event_personnel WHERE personnel_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, personnelId);
            ps.executeUpdate();
        }
    }

    private void bindPersonnel(PreparedStatement ps, EventPersonnel personnel) throws SQLException {
        ps.setString(1, personnel.getFirstName());
        ps.setString(2, personnel.getLastName());
        ps.setString(3, personnel.getAvailabilityStatus());
        ps.setString(4, personnel.getRole());
        ps.setString(5, personnel.getAffiliation());

        if (personnel.getContactNo() != null && !personnel.getContactNo().trim().isEmpty()) {
            ps.setString(6, personnel.getContactNo().trim());
        } else {
            ps.setNull(6, Types.VARCHAR);
        }

        ps.setInt(7, personnel.getEventId());

        if (personnel.getMatchId() != null) {
            ps.setInt(8, personnel.getMatchId());
        } else {
            ps.setNull(8, Types.INTEGER);
        }
    }
}
