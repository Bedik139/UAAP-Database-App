import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamDAO {

    private static final List<String> DEFAULT_UAAP_TEAMS = Collections.unmodifiableList(Arrays.asList(
            "De La Salle Green Archers",
            "Ateneo Blue Eagles",
            "UP Fighting Maroons",
            "UST Growling Tigers",
            "Far Eastern University Tamaraws",
            "University of the East Red Warriors",
            "National University Bulldogs",
            "Adamson Soaring Falcons"
    ));

    public static List<String> getAllowedTeamNames() {
        return DEFAULT_UAAP_TEAMS;
    }

    public void insertTeam(Team team) throws SQLException {
        String sql = "INSERT INTO team " +
                "(team_name, gender, sport, seasons_played, standing_wins, standing_losses, total_games_played) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getGender());
            ps.setString(3, team.getSport());
            ps.setInt(4, team.getSeasonsPlayed());
            ps.setInt(5, team.getStandingWins());
            ps.setInt(6, team.getStandingLosses());
            ps.setInt(7, team.getTotalGamesPlayed());

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

        String sql = "SELECT team_id, team_name, gender, sport, seasons_played, " +
                "standing_wins, standing_losses, total_games_played " +
                "FROM team ORDER BY team_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Team team = new Team(
                        rs.getInt("team_id"),
                        rs.getString("team_name"),
                        rs.getString("gender"),
                        rs.getString("sport"),
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

    private void seedDefaultTeamsIfMissing() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            Set<String> existing = new HashSet<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT team_name FROM team");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existing.add(rs.getString("team_name"));
                }
            }

            List<String> missing = new ArrayList<>();
            for (String name : DEFAULT_UAAP_TEAMS) {
                if (!existing.contains(name)) {
                    missing.add(name);
                }
            }
            if (missing.isEmpty()) {
                return;
            }

            String sql = "INSERT INTO team " +
                    "(team_name, seasons_played, standing_wins, standing_losses, total_games_played) " +
                    "VALUES (?, 0, 0, 0, 0)";
            try (PreparedStatement insert = conn.prepareStatement(sql)) {
                for (String name : missing) {
                    insert.setString(1, name);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        }
    }

    public void updateTeam(Team team) throws SQLException {
        String sql = "UPDATE team SET " +
                "team_name = ?, gender = ?, sport = ?, seasons_played = ?, standing_wins = ?, " +
                "standing_losses = ?, total_games_played = ? " +
                "WHERE team_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getGender());
            ps.setString(3, team.getSport());
            ps.setInt(4, team.getSeasonsPlayed());
            ps.setInt(5, team.getStandingWins());
            ps.setInt(6, team.getStandingLosses());
            ps.setInt(7, team.getTotalGamesPlayed());
            ps.setInt(8, team.getTeamId());

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
