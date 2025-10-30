import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO {

    public void insertMatch(Match match) throws SQLException {
        String sql = "INSERT INTO `match` " +
                "(event_id, match_type, match_date, match_time_start, match_time_end) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindMatch(ps, match);
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

        String sql = "SELECT m.match_id, m.event_id, e.event_name, m.match_type, " +
                "m.match_date, m.match_time_start, m.match_time_end " +
                "FROM `match` m INNER JOIN event e ON m.event_id = e.event_id " +
                "ORDER BY m.match_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Match match = new Match(
                        rs.getInt("match_id"),
                        rs.getInt("event_id"),
                        rs.getString("event_name"),
                        rs.getString("match_type"),
                        rs.getDate("match_date"),
                        rs.getTime("match_time_start"),
                        rs.getTime("match_time_end")
                );
                matches.add(match);
            }
        }

        return matches;
    }

    public Match getMatchById(int matchId) throws SQLException {
        String sql = "SELECT m.match_id, m.event_id, e.event_name, m.match_type, " +
                "m.match_date, m.match_time_start, m.match_time_end " +
                "FROM `match` m INNER JOIN event e ON m.event_id = e.event_id " +
                "WHERE m.match_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Match(
                            rs.getInt("match_id"),
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getString("match_type"),
                            rs.getDate("match_date"),
                            rs.getTime("match_time_start"),
                            rs.getTime("match_time_end")
                    );
                }
            }
        }

        return null;
    }

    public void updateMatch(Match match) throws SQLException {
        String sql = "UPDATE `match` SET " +
                "event_id = ?, match_type = ?, match_date = ?, " +
                "match_time_start = ?, match_time_end = ? " +
                "WHERE match_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindMatch(ps, match);
            ps.setInt(6, match.getMatchId());

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

        Date matchDate = match.getMatchDate();
        Time start = match.getMatchTimeStart();
        Time end = match.getMatchTimeEnd();

        ps.setDate(3, matchDate);
        ps.setTime(4, start);
        ps.setTime(5, end);
    }
}
