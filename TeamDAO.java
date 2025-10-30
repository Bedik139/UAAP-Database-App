import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TeamDAO {

    public void insertTeam(Team team) throws SQLException {
        String sql = "INSERT INTO team " +
                "(team_name, seasons_played, standing_wins, standing_losses, total_games_played) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, team.getTeamName());
            ps.setInt(2, team.getSeasonsPlayed());
            ps.setInt(3, team.getStandingWins());
            ps.setInt(4, team.getStandingLosses());
            ps.setInt(5, team.getTotalGamesPlayed());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    team.setTeamId(rs.getInt(1));
                }
            }
        }
    }

    public List<Team> getAllTeams() throws SQLException {
        List<Team> teams = new ArrayList<>();

        String sql = "SELECT team_id, team_name, seasons_played, " +
                "standing_wins, standing_losses, total_games_played " +
                "FROM team ORDER BY team_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Team team = new Team(
                        rs.getInt("team_id"),
                        rs.getString("team_name"),
                        rs.getInt("seasons_played"),
                        rs.getInt("standing_wins"),
                        rs.getInt("standing_losses"),
                        rs.getInt("total_games_played")
                );
                teams.add(team);
            }
        }

        return teams;
    }

    public void updateTeam(Team team) throws SQLException {
        String sql = "UPDATE team SET " +
                "team_name = ?, seasons_played = ?, standing_wins = ?, " +
                "standing_losses = ?, total_games_played = ? " +
                "WHERE team_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, team.getTeamName());
            ps.setInt(2, team.getSeasonsPlayed());
            ps.setInt(3, team.getStandingWins());
            ps.setInt(4, team.getStandingLosses());
            ps.setInt(5, team.getTotalGamesPlayed());
            ps.setInt(6, team.getTeamId());

            ps.executeUpdate();
        }
    }

    public void deleteTeam(int teamId) throws SQLException {
        String sql = "DELETE FROM team WHERE team_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teamId);
            ps.executeUpdate();
        }
    }
}
