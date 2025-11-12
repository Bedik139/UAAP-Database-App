import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    public void insertPlayer(Player player) throws SQLException {
        String sql = "INSERT INTO player " +
                "(team_id, player_first_name, player_last_name, player_number, player_sport, " +
                "age, position, weight, height, individual_score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindPlayer(ps, player);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    player.setPlayerId(rs.getInt(1));
                }
            }
        }
    }

    public List<Player> getAllPlayers() throws SQLException {
        String sql = "SELECT p.player_id, p.team_id, t.team_name, " +
                "p.player_first_name, p.player_last_name, p.player_number, p.player_sport, " +
                "p.age, p.position, p.weight, p.height, p.individual_score " +
                "FROM player p INNER JOIN team t ON p.team_id = t.team_id " +
                "ORDER BY p.player_id";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapPlayers(rs);
        }
    }

    public List<Player> getPlayersByTeam(int teamId) throws SQLException {
        String sql = "SELECT p.player_id, p.team_id, t.team_name, " +
                "p.player_first_name, p.player_last_name, p.player_number, p.player_sport, " +
                "p.age, p.position, p.weight, p.height, p.individual_score " +
                "FROM player p INNER JOIN team t ON p.team_id = t.team_id " +
                "WHERE p.team_id = ? ORDER BY p.player_last_name, p.player_first_name";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapPlayers(rs);
            }
        }
    }

    public List<Player> getPlayersBySport(String sport) throws SQLException {
        String sql = "SELECT p.player_id, p.team_id, t.team_name, " +
                "p.player_first_name, p.player_last_name, p.player_number, p.player_sport, " +
                "p.age, p.position, p.weight, p.height, p.individual_score " +
                "FROM player p INNER JOIN team t ON p.team_id = t.team_id " +
                "WHERE p.player_sport = ? ORDER BY p.player_last_name, p.player_first_name";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sport);
            try (ResultSet rs = ps.executeQuery()) {
                return mapPlayers(rs);
            }
        }
    }

    public void updatePlayer(Player player) throws SQLException {
        String sql = "UPDATE player SET " +
                "team_id = ?, player_first_name = ?, player_last_name = ?, player_number = ?, player_sport = ?, " +
                "age = ?, position = ?, weight = ?, height = ?, individual_score = ? " +
                "WHERE player_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindPlayer(ps, player);
            ps.setInt(11, player.getPlayerId());
            ps.executeUpdate();
        }
    }

    public void deletePlayer(int playerId) throws SQLException {
        String sql = "DELETE FROM player WHERE player_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ps.executeUpdate();
        }
    }

    private List<Player> mapPlayers(ResultSet rs) throws SQLException {
        List<Player> players = new ArrayList<>();
        while (rs.next()) {
            int ageValue = rs.getInt("age");
            Integer age = rs.wasNull() ? null : ageValue;

            Player player = new Player(
                    rs.getInt("player_id"),
                    rs.getInt("team_id"),
                    rs.getString("team_name"),
                    rs.getString("player_first_name"),
                    rs.getString("player_last_name"),
                    rs.getInt("player_number"),
                    rs.getString("player_sport"),
                    age,
                    rs.getString("position"),
                    rs.getBigDecimal("weight"),
                    rs.getBigDecimal("height"),
                    rs.getInt("individual_score")
            );
            players.add(player);
        }
        return players;
    }

    private void bindPlayer(PreparedStatement ps, Player player) throws SQLException {
        ps.setInt(1, player.getTeamId());
        ps.setString(2, player.getFirstName());
        ps.setString(3, player.getLastName());
        ps.setInt(4, player.getPlayerNumber());
        ps.setString(5, player.getSport());

        if (player.getAge() != null) {
            ps.setInt(6, player.getAge());
        } else {
            ps.setNull(6, Types.INTEGER);
        }

        if (player.getPosition() != null && !player.getPosition().trim().isEmpty()) {
            ps.setString(7, player.getPosition().trim());
        } else {
            ps.setNull(7, Types.VARCHAR);
        }

        if (player.getWeight() != null) {
            ps.setBigDecimal(8, player.getWeight());
        } else {
            ps.setNull(8, Types.DECIMAL);
        }

        if (player.getHeight() != null) {
            ps.setBigDecimal(9, player.getHeight());
        } else {
            ps.setNull(9, Types.DECIMAL);
        }

        // This will be overridden for updates, kept for inserts.
        ps.setInt(10, player.getIndividualScore());
    }
}
