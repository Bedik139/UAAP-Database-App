import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MatchResultService {

    public MatchResult recordResult(MatchResultInput input) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                Match match = fetchMatch(conn, input.matchId(), true);
                if (match == null) {
                    throw new IllegalArgumentException("Match not found.");
                }

                if ("Completed".equalsIgnoreCase(match.getStatus())) {
                    throw new IllegalStateException("Match already marked as completed.");
                }

                List<MatchTeamRow> participants = fetchMatchTeams(conn, input.matchId(), true);
                if (participants.size() != 2) {
                    throw new IllegalStateException("Match must have exactly two participating teams.");
                }

                MatchTeamRow home = participants.stream()
                        .filter(MatchTeamRow::isHome)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Home team not configured for match."));
                MatchTeamRow away = participants.stream()
                        .filter(row -> !row.isHome())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Away team not configured for match."));

                updateTeamScore(conn, input.matchId(), home.teamId(), input.homeScore());
                updateTeamScore(conn, input.matchId(), away.teamId(), input.awayScore());

                boolean isTie = input.homeScore() == input.awayScore();
                Integer winningTeam = null;
                Integer losingTeam = null;

                if (!isTie) {
                    boolean homeWon = input.homeScore() > input.awayScore();
                    winningTeam = homeWon ? home.teamId() : away.teamId();
                    losingTeam = homeWon ? away.teamId() : home.teamId();
                    applyTeamResult(conn, winningTeam, true);
                    applyTeamResult(conn, losingTeam, false);
                } else {
                    applyTeamDraw(conn, home.teamId());
                    applyTeamDraw(conn, away.teamId());
                }

                updateMatchMetadata(conn, input.matchId(), input.scoreSummary(), input.processedBy());

                if (!input.playerUpdates().isEmpty()) {
                    applyPlayerStats(conn, input.playerUpdates());
                }

                conn.commit();
                conn.setAutoCommit(originalAutoCommit);

                return new MatchResult(
                        input.matchId(),
                        home.teamId(),
                        away.teamId(),
                        input.homeScore(),
                        input.awayScore(),
                        winningTeam,
                        losingTeam,
                        isTie,
                        input.scoreSummary(),
                        Timestamp.valueOf(LocalDateTime.now()),
                        input.processedBy()
                );
            } catch (Exception ex) {
                conn.rollback();
                throw ex instanceof SQLException ? (SQLException) ex : new SQLException(ex.getMessage(), ex);
            }
        }
    }

    private Match fetchMatch(Connection conn, int matchId, boolean forUpdate) throws SQLException {
        String sql = "SELECT m.match_id, m.event_id, e.event_name, m.match_type, m.match_date, " +
                "m.match_time_start, m.match_time_end, m.status, m.score_summary " +
                "FROM `match` m INNER JOIN event e ON m.event_id = e.event_id " +
                "WHERE m.match_id = ? " + (forUpdate ? "FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
                            rs.getTime("match_time_end"),
                            rs.getString("status"),
                            rs.getString("score_summary")
                    );
                }
            }
        }
        return null;
    }

    private List<MatchTeamRow> fetchMatchTeams(Connection conn, int matchId, boolean forUpdate) throws SQLException {
        List<MatchTeamRow> rows = new ArrayList<>();
        String sql = "SELECT match_id, team_id, is_home, team_score FROM match_team WHERE match_id = ? " +
                (forUpdate ? "FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new MatchTeamRow(
                            rs.getInt("match_id"),
                            rs.getInt("team_id"),
                            rs.getBoolean("is_home"),
                            rs.getInt("team_score")
                    ));
                }
            }
        }

        return rows;
    }

    private void updateTeamScore(Connection conn, int matchId, int teamId, int newScore) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE match_team SET team_score = ? WHERE match_id = ? AND team_id = ?")) {
            ps.setInt(1, newScore);
            ps.setInt(2, matchId);
            ps.setInt(3, teamId);
            ps.executeUpdate();
        }
    }

    private void applyTeamResult(Connection conn, int teamId, boolean isWin) throws SQLException {
        if (isWin) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE team SET standing_wins = standing_wins + 1, total_games_played = total_games_played + 1 " +
                            "WHERE team_id = ?")) {
                ps.setInt(1, teamId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE team SET standing_losses = standing_losses + 1, total_games_played = total_games_played + 1 " +
                            "WHERE team_id = ?")) {
                ps.setInt(1, teamId);
                ps.executeUpdate();
            }
        }
    }

    private void applyTeamDraw(Connection conn, int teamId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE team SET total_games_played = total_games_played + 1 WHERE team_id = ?")) {
            ps.setInt(1, teamId);
            ps.executeUpdate();
        }
    }

    private void updateMatchMetadata(Connection conn, int matchId, String scoreSummary, String processedBy) throws SQLException {
        String summary = scoreSummary != null && !scoreSummary.isBlank()
                ? scoreSummary
                : "Updated by " + (processedBy != null ? processedBy : "system") + " on " + LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE `match` SET status = 'Completed', score_summary = ? WHERE match_id = ?")) {
            ps.setString(1, summary);
            ps.setInt(2, matchId);
            ps.executeUpdate();
        }
    }

    private void applyPlayerStats(Connection conn, List<PlayerStatUpdate> updates) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE player SET individual_score = individual_score + ? WHERE player_id = ?")) {
            for (PlayerStatUpdate update : updates) {
                ps.setInt(1, update.pointsEarned());
                ps.setInt(2, update.playerId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public record MatchResultInput(int matchId,
                                   int homeScore,
                                   int awayScore,
                                   String scoreSummary,
                                   String processedBy,
                                   List<PlayerStatUpdate> playerUpdates) {}

    public record PlayerStatUpdate(int playerId, int pointsEarned) {}

    public record MatchResult(int matchId,
                              int homeTeamId,
                              int awayTeamId,
                              int homeScore,
                              int awayScore,
                              Integer winningTeamId,
                              Integer losingTeamId,
                              boolean tie,
                              String scoreSummary,
                              Timestamp processedTimestamp,
                              String processedBy) {}

    private record MatchTeamRow(int matchId, int teamId, boolean isHome, int score) {}
}
