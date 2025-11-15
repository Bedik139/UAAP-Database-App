import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO {

    private static final String BASE_SELECT =
            "SELECT m.match_id, m.event_id, e.event_name, m.match_type, " +
            "m.match_time_start, m.match_time_end, m.status, m.score_summary, " +
            "e.match_date AS event_match_date " +
            "FROM `match` m INNER JOIN event e ON m.event_id = e.event_id ";

    public void insertMatch(Match match) throws SQLException {
        String sql = "INSERT INTO `match` " +
                "(event_id, match_type, match_time_start, match_time_end, status, score_summary) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            validateMatchConstraints(conn, match, null);
            bindMatch(ps, match);
            ps.setString(5, match.getStatus());
            ps.setString(6, match.getScoreSummary());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    match.setMatchId(rs.getInt(1));
                }
            }
        }
    }

    public List<Match> getAllMatches() throws SQLException {
        List<Match> matches = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY m.match_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                matches.add(mapRow(rs));
            }
        }

        return matches;
    }

    public Match getMatchById(int matchId) throws SQLException {
        String sql = BASE_SELECT + "WHERE m.match_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    public void updateMatch(Match match) throws SQLException {
        String sql = "UPDATE `match` SET event_id = ?, match_type = ?, " +
                "match_time_start = ?, match_time_end = ?, status = ?, score_summary = ? " +
                "WHERE match_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            validateMatchConstraints(conn, match, match.getMatchId());
            bindMatch(ps, match);
            ps.setString(5, match.getStatus());
            ps.setString(6, match.getScoreSummary());
            ps.setInt(7, match.getMatchId());

            ps.executeUpdate();
        }
    }

    public void updateMatchStatus(int matchId, String status, String scoreSummary) throws SQLException {
        String sql = "UPDATE `match` SET status = ?, score_summary = ? WHERE match_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, scoreSummary);
            ps.setInt(3, matchId);
            ps.executeUpdate();
        }
    }

    public void deleteMatch(int matchId) throws SQLException {
        String sql = "DELETE FROM `match` WHERE match_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);
            ps.executeUpdate();
        }
    }

    private void bindMatch(PreparedStatement ps, Match match) throws SQLException {
        ps.setInt(1, match.getEventId());
        ps.setString(2, match.getMatchType());
        ps.setTime(3, match.getMatchTimeStart());
        ps.setTime(4, match.getMatchTimeEnd());
    }

    private Match mapRow(ResultSet rs) throws SQLException {
        return new Match(
                rs.getInt("match_id"),
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("match_type"),
                rs.getTime("match_time_start"),
                rs.getTime("match_time_end"),
                rs.getString("status"),
                rs.getString("score_summary"),
                rs.getDate("event_match_date")
        );
    }

    private void validateMatchConstraints(Connection conn, Match match, Integer excludeMatchId) throws SQLException {
        EventWindow window = fetchEventWindow(conn, match.getEventId());
        if (window == null) {
            throw new IllegalArgumentException("Event not found for the selected match.");
        }
        if (match.getMatchTimeStart() == null || match.getMatchTimeEnd() == null ||
                !match.getMatchTimeEnd().after(match.getMatchTimeStart())) {
            throw new IllegalArgumentException("Match end time must be after the start time.");
        }
        if (match.getMatchTimeStart().before(window.eventStart()) ||
                match.getMatchTimeEnd().after(window.eventEnd())) {
            throw new IllegalArgumentException("Match start and end must fall within the parent event's timeframe.");
        }
        ensureNoOverlap(conn, match, excludeMatchId);
    }

    private EventWindow fetchEventWindow(Connection conn, int eventId) throws SQLException {
        String sql = "SELECT match_date, event_time_start, event_time_end, sport FROM event WHERE event_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new EventWindow(
                            rs.getDate("match_date"),
                            rs.getTime("event_time_start"),
                            rs.getTime("event_time_end"),
                            rs.getString("sport")
                    );
                }
            }
        }
        return null;
    }

    private void ensureNoOverlap(Connection conn, Match match, Integer excludeMatchId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT match_time_start, match_time_end FROM `match` WHERE event_id = ?");
        if (excludeMatchId != null) {
            sql.append(" AND match_id <> ?");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, match.getEventId());
            if (excludeMatchId != null) {
                ps.setInt(2, excludeMatchId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time existingStart = rs.getTime("match_time_start");
                    Time existingEnd = rs.getTime("match_time_end");
                    if (timesOverlap(match.getMatchTimeStart(), match.getMatchTimeEnd(), existingStart, existingEnd)) {
                        throw new IllegalArgumentException("Match times overlap with another match under the same event.");
                    }
                }
            }
        }
    }

    private boolean timesOverlap(Time startA, Time endA, Time startB, Time endB) {
        return startA.before(endB) && endA.after(startB);
    }

    private record EventWindow(Date matchDate, Time eventStart, Time eventEnd, String sport) {}
}
