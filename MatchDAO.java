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
            "SELECT m.match_id, m.event_id, e.event_name, m.match_type, m.match_date, " +
            "m.match_time_start, m.match_time_end, m.status, m.score_summary " +
            "FROM `match` m INNER JOIN event e ON m.event_id = e.event_id ";

    public void insertMatch(Match match) throws SQLException {
        String sql = "INSERT INTO `match` " +
                "(event_id, match_type, match_date, match_time_start, match_time_end, status, score_summary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindMatch(ps, match);
            ps.setString(6, match.getStatus());
            ps.setString(7, match.getScoreSummary());
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
        String sql = "UPDATE `match` SET event_id = ?, match_type = ?, match_date = ?, " +
                "match_time_start = ?, match_time_end = ?, status = ?, score_summary = ? " +
                "WHERE match_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindMatch(ps, match);
            ps.setString(6, match.getStatus());
            ps.setString(7, match.getScoreSummary());
            ps.setInt(8, match.getMatchId());

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
        ps.setDate(3, match.getMatchDate());
        ps.setTime(4, match.getMatchTimeStart());
        ps.setTime(5, match.getMatchTimeEnd());
    }

    private Match mapRow(ResultSet rs) throws SQLException {
        return new Match(
                rs.getInt("match_id"),
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("match_type"),
                rs.getDate("match_date"),
                rs.getTime("match_time_start"),
                rs.getTime("match_time_end"),
                rs.getString("status"),
                rs.getString("score_summary")
        );
    }
}
