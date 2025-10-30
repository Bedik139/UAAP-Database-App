import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatchSetScoreDAO {

    public void insertScore(MatchSetScore score) throws SQLException {
        String sql = "INSERT INTO match_set_score (match_id, team_id, set_no, points) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindScore(ps, score);
            ps.executeUpdate();
        }
    }

    public List<MatchSetScore> getScoresForMatch(int matchId) throws SQLException {
        List<MatchSetScore> list = new ArrayList<>();

        String sql = "SELECT mss.match_id, mss.team_id, mss.set_no, mss.points, " +
                "CONCAT('#', m.match_id, ' ', e.event_name, ' (', m.match_type, ')') AS match_label, " +
                "t.team_name " +
                "FROM match_set_score mss " +
                "INNER JOIN `match` m ON mss.match_id = m.match_id " +
                "INNER JOIN event e ON m.event_id = e.event_id " +
                "INNER JOIN team t ON mss.team_id = t.team_id " +
                "WHERE mss.match_id = ? " +
                "ORDER BY mss.team_id, mss.set_no";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchSetScore score = new MatchSetScore(
                            rs.getInt("match_id"),
                            rs.getInt("team_id"),
                            rs.getInt("set_no"),
                            rs.getInt("points"),
                            rs.getString("match_label"),
                            rs.getString("team_name")
                    );
                    list.add(score);
                }
            }
        }

        return list;
    }

    public void updateScore(MatchSetScore score,
                            int originalMatchId,
                            int originalTeamId,
                            int originalSetNo) throws SQLException {
        String sql = "UPDATE match_set_score SET match_id = ?, team_id = ?, set_no = ?, points = ? " +
                "WHERE match_id = ? AND team_id = ? AND set_no = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindScore(ps, score);
            ps.setInt(5, originalMatchId);
            ps.setInt(6, originalTeamId);
            ps.setInt(7, originalSetNo);
            ps.executeUpdate();
        }
    }

    public void deleteScore(int matchId, int teamId, int setNo) throws SQLException {
        String sql = "DELETE FROM match_set_score WHERE match_id = ? AND team_id = ? AND set_no = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);
            ps.setInt(2, teamId);
            ps.setInt(3, setNo);
            ps.executeUpdate();
        }
    }

    private void bindScore(PreparedStatement ps, MatchSetScore score) throws SQLException {
        ps.setInt(1, score.getMatchId());
        ps.setInt(2, score.getTeamId());
        ps.setInt(3, score.getSetNo());
        ps.setInt(4, score.getPoints());
    }
}
