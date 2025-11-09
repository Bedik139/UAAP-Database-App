import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatchTeamDAO {

    public void insertMatchTeam(MatchTeam entry) throws SQLException {
        String sql = "INSERT INTO match_team (match_id, team_id, is_home, team_score) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, entry.getMatchId());
            ps.setInt(2, entry.getTeamId());
            ps.setBoolean(3, entry.isHomeTeam());
            ps.setInt(4, entry.getTeamScore());
            ps.executeUpdate();
        }
    }

    public List<MatchTeam> getAllMatchTeams() throws SQLException {
        List<MatchTeam> list = new ArrayList<>();

        String sql = "SELECT mt.match_id, mt.team_id, mt.is_home, mt.team_score, " +
                "CONCAT('#', m.match_id, ' ', e.event_name, ' (', m.match_type, ')') AS match_label, " +
                "t.team_name " +
                "FROM match_team mt " +
                "INNER JOIN `match` m ON mt.match_id = m.match_id " +
                "INNER JOIN event e ON m.event_id = e.event_id " +
                "INNER JOIN team t ON mt.team_id = t.team_id " +
                "ORDER BY mt.match_id, mt.team_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MatchTeam entry = new MatchTeam(
                        rs.getInt("match_id"),
                        rs.getInt("team_id"),
                        rs.getBoolean("is_home"),
                        rs.getInt("team_score"),
                        rs.getString("match_label"),
                        rs.getString("team_name")
                );
                list.add(entry);
            }
        }

        return list;
    }

    public List<MatchTeam> getMatchTeamsForMatch(int matchId) throws SQLException {
        List<MatchTeam> list = new ArrayList<>();

        String sql = "SELECT mt.match_id, mt.team_id, mt.is_home, mt.team_score, " +
                "CONCAT('#', m.match_id, ' ', e.event_name, ' (', m.match_type, ')') AS match_label, " +
                "t.team_name " +
                "FROM match_team mt " +
                "INNER JOIN `match` m ON mt.match_id = m.match_id " +
                "INNER JOIN event e ON m.event_id = e.event_id " +
                "INNER JOIN team t ON mt.team_id = t.team_id " +
                "WHERE mt.match_id = ? " +
                "ORDER BY mt.team_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchTeam entry = new MatchTeam(
                            rs.getInt("match_id"),
                            rs.getInt("team_id"),
                            rs.getBoolean("is_home"),
                            rs.getInt("team_score"),
                            rs.getString("match_label"),
                            rs.getString("team_name")
                    );
                    list.add(entry);
                }
            }
        }

        return list;
    }

    public void updateMatchTeam(MatchTeam entry, int originalMatchId, int originalTeamId) throws SQLException {
        String sql = "UPDATE match_team SET match_id = ?, team_id = ?, is_home = ?, team_score = ? " +
                "WHERE match_id = ? AND team_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, entry.getMatchId());
            ps.setInt(2, entry.getTeamId());
            ps.setBoolean(3, entry.isHomeTeam());
            ps.setInt(4, entry.getTeamScore());
            ps.setInt(5, originalMatchId);
            ps.setInt(6, originalTeamId);

            ps.executeUpdate();
        }
    }

    public void deleteMatchTeam(int matchId, int teamId) throws SQLException {
        String sql = "DELETE FROM match_team WHERE match_id = ? AND team_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);
            ps.setInt(2, teamId);
            ps.executeUpdate();
        }
    }

    public void updateTeamScore(int matchId, int teamId, int teamScore) throws SQLException {
        String sql = "UPDATE match_team SET team_score = ? WHERE match_id = ? AND team_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teamScore);
            ps.setInt(2, matchId);
            ps.setInt(3, teamId);
            ps.executeUpdate();
        }
    }
}
