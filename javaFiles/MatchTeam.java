public class MatchTeam {

    private int matchId;
    private int teamId;
    private boolean homeTeam;
    private int teamScore;
    private String matchLabel;
    private String teamName;

    public MatchTeam() {}

    public MatchTeam(int matchId,
                     int teamId,
                     boolean homeTeam,
                     int teamScore,
                     String matchLabel,
                     String teamName) {
        this.matchId = matchId;
        this.teamId = teamId;
        this.homeTeam = homeTeam;
        this.teamScore = teamScore;
        this.matchLabel = matchLabel;
        this.teamName = teamName;
    }

    public MatchTeam(int matchId, int teamId, boolean homeTeam, int teamScore) {
        this(matchId, teamId, homeTeam, teamScore, null, null);
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public boolean isHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(boolean homeTeam) {
        this.homeTeam = homeTeam;
    }

    public int getTeamScore() {
        return teamScore;
    }

    public void setTeamScore(int teamScore) {
        this.teamScore = teamScore;
    }

    public String getMatchLabel() {
        return matchLabel;
    }

    public void setMatchLabel(String matchLabel) {
        this.matchLabel = matchLabel;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
