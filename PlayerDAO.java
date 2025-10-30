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
                "(team_id, player_first_name, player_last_name, player_number, " +
                "age, position, weight, height, individual_score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        List<Player> players = new ArrayList<>();

        String sql = "SELECT p.player_id, p.team_id, t.team_name, " +
                "p.player_first_name, p.player_last_name, p.player_number, " +
                "p.age, p.position, p.weight, p.height, p.individual_score " +
                "FROM player p INNER JOIN team t ON p.team_id = t.team_id " +
                "ORDER BY p.player_id";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
                        age,
                        rs.getString("position"),
                        rs.getBigDecimal("weight"),
                        rs.getBigDecimal("height"),
                        rs.getInt("individual_score")
                );
                players.add(player);
            }
        }

        return players;
    }

    public void updatePlayer(Player player) throws SQLException {
        String sql = "UPDATE player SET " +
                "team_id = ?, player_first_name = ?, player_last_name = ?, player_number = ?, " +
                "age = ?, position = ?, weight = ?, height = ?, individual_score = ? " +
                "WHERE player_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindPlayer(ps, player);
            ps.setInt(10, player.getPlayerId());
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

    private void bindPlayer(PreparedStatement ps, Player player) throws SQLException {
        ps.setInt(1, player.getTeamId());
        ps.setString(2, player.getFirstName());
        ps.setString(3, player.getLastName());
        ps.setInt(4, player.getPlayerNumber());

        if (player.getAge() != null) {
            ps.setInt(5, player.getAge());
        } else {
            ps.setNull(5, Types.INTEGER);
        }

        if (player.getPosition() != null && !player.getPosition().trim().isEmpty()) {
            ps.setString(6, player.getPosition().trim());
        } else {
            ps.setNull(6, Types.VARCHAR);
        }

        if (player.getWeight() != null) {
            ps.setBigDecimal(7, player.getWeight());
        } else {
            ps.setNull(7, Types.DECIMAL);
        }

        if (player.getHeight() != null) {
            ps.setBigDecimal(8, player.getHeight());
        } else {
            ps.setNull(8, Types.DECIMAL);
        }

        // This will be overridden for updates, kept for inserts.
        ps.setInt(9, player.getIndividualScore());
    }
}
