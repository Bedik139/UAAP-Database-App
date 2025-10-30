public class MatchQuarterScore {

    private int matchId;
    private int teamId;
    private int quarterNo;
    private int points;
    private String matchLabel;
    private String teamName;

    public MatchQuarterScore() {}

    public MatchQuarterScore(int matchId,
                             int teamId,
                             int quarterNo,
                             int points,
                             String matchLabel,
                             String teamName) {
        this.matchId = matchId;
        this.teamId = teamId;
        this.quarterNo = quarterNo;
        this.points = points;
        this.matchLabel = matchLabel;
        this.teamName = teamName;
    }

    public MatchQuarterScore(int matchId, int teamId, int quarterNo, int points) {
        this(matchId, teamId, quarterNo, points, null, null);
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

    public int getQuarterNo() {
        return quarterNo;
    }

    public void setQuarterNo(int quarterNo) {
        this.quarterNo = quarterNo;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
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
