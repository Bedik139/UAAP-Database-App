import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consolidates granular quarter/set scoring into match_team totals and the match score summary.
 */
public class ScoreAggregationService {

    private final MatchDAO matchDAO = new MatchDAO();
    private final MatchTeamDAO matchTeamDAO = new MatchTeamDAO();
    private final MatchQuarterScoreDAO quarterScoreDAO = new MatchQuarterScoreDAO();
    private final MatchSetScoreDAO setScoreDAO = new MatchSetScoreDAO();
    private final EventDAO eventDAO = new EventDAO();

    public void updateTotalsForMatch(int matchId) throws SQLException {
        Match match = matchDAO.getMatchById(matchId);
        if (match == null) {
            return;
        }

        Event event = eventDAO.getEventById(match.getEventId());
        if (event == null) {
            return;
        }

        List<MatchTeam> teams = matchTeamDAO.getMatchTeamsForMatch(matchId);
        if (teams.isEmpty()) {
            return;
        }

        Map<Integer, Integer> totals = determineTotals(matchId, event.getSport());
        if (totals == null) {
            return;
        }

        for (MatchTeam team : teams) {
            int total = totals.getOrDefault(team.getTeamId(), 0);
            if (team.getTeamScore() != total) {
                matchTeamDAO.updateTeamScore(team.getMatchId(), team.getTeamId(), total);
                team.setTeamScore(total);
            } else {
                team.setTeamScore(total);
            }
        }

        String summary = buildSummary(teams);
        matchDAO.updateMatchStatus(matchId, match.getStatus(), summary);
    }

    private Map<Integer, Integer> determineTotals(int matchId, String sport) throws SQLException {
        if (sport == null) {
            return null;
        }
        String normalized = sport.toLowerCase();
        if (normalized.contains("basket")) {
            return aggregateQuarterScores(matchId);
        }
        if (normalized.contains("volley")) {
            return aggregateSetScores(matchId);
        }
        return null;
    }

    private Map<Integer, Integer> aggregateQuarterScores(int matchId) throws SQLException {
        Map<Integer, Integer> totals = new HashMap<>();
        for (MatchQuarterScore score : quarterScoreDAO.getScoresForMatch(matchId)) {
            totals.merge(score.getTeamId(), score.getQuarterPoints(), Integer::sum);
        }
        return totals;
    }

    private Map<Integer, Integer> aggregateSetScores(int matchId) throws SQLException {
        Map<Integer, Integer> totals = new HashMap<>();
        for (MatchSetScore score : setScoreDAO.getScoresForMatch(matchId)) {
            totals.merge(score.getTeamId(), score.getSetPoints(), Integer::sum);
        }
        return totals;
    }

    private String buildSummary(List<MatchTeam> teams) {
        if (teams.isEmpty()) {
            return null;
        }
        List<MatchTeam> ordered = new ArrayList<>(teams);
        ordered.sort(Comparator.comparing((MatchTeam team) -> !team.isHomeTeam()));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ordered.size(); i++) {
            MatchTeam team = ordered.get(i);
            builder.append(team.getTeamName())
                    .append(' ')
                    .append(team.getTeamScore());
            if (i < ordered.size() - 1) {
                builder.append(" - ");
            }
        }
        return builder.toString();
    }
}
