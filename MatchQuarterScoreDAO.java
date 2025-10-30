import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatchQuarterScoreDAO {

    public void insertScore(MatchQuarterScore score) throws SQLException {
        String sql = "INSERT INTO match_quarter_score (match_id, team_id, quarter_no, points) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindScore(ps, score);
            ps.executeUpdate();
        }
    }

    public List<MatchQuarterScore> getScoresForMatch(int matchId) throws SQLException {
        List<MatchQuarterScore> list = new ArrayList<>();

        String sql = "SELECT mqs.match_id, mqs.team_id, mqs.quarter_no, mqs.points, " +
                "CONCAT('#', m.match_id, ' ', e.event_name, ' (', m.match_type, ')') AS match_label, " +
                "t.team_name " +
                "FROM match_quarter_score mqs " +
                "INNER JOIN `match` m ON mqs.match_id = m.match_id " +
                "INNER JOIN event e ON m.event_id = e.event_id " +
                "INNER JOIN team t ON mqs.team_id = t.team_id " +
                "WHERE mqs.match_id = ? " +
                "ORDER BY mqs.team_id, mqs.quarter_no";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchQuarterScore score = new MatchQuarterScore(
                            rs.getInt("match_id"),
                            rs.getInt("team_id"),
                            rs.getInt("quarter_no"),
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

    public void updateScore(MatchQuarterScore score,
                            int originalMatchId,
                            int originalTeamId,
                            int originalQuarter) throws SQLException {
        String sql = "UPDATE match_quarter_score SET match_id = ?, team_id = ?, quarter_no = ?, points = ? " +
                "WHERE match_id = ? AND team_id = ? AND quarter_no = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindScore(ps, score);
            ps.setInt(5, originalMatchId);
            ps.setInt(6, originalTeamId);
            ps.setInt(7, originalQuarter);
            ps.executeUpdate();
        }
    }

    public void deleteScore(int matchId, int teamId, int quarterNo) throws SQLException {
        String sql = "DELETE FROM match_quarter_score WHERE match_id = ? AND team_id = ? AND quarter_no = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);
            ps.setInt(2, teamId);
            ps.setInt(3, quarterNo);
            ps.executeUpdate();
        }
    }

    private void bindScore(PreparedStatement ps, MatchQuarterScore score) throws SQLException {
        ps.setInt(1, score.getMatchId());
        ps.setInt(2, score.getTeamId());
        ps.setInt(3, score.getQuarterNo());
        ps.setInt(4, score.getPoints());
    }
}
