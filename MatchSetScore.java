public class MatchSetScore {

    private int matchId;
    private int teamId;
    private int setNo;
    private int points;
    private String matchLabel;
    private String teamName;

    public MatchSetScore() {}

    public MatchSetScore(int matchId,
                         int teamId,
                         int setNo,
                         int points,
                         String matchLabel,
                         String teamName) {
        this.matchId = matchId;
        this.teamId = teamId;
        this.setNo = setNo;
        this.points = points;
        this.matchLabel = matchLabel;
        this.teamName = teamName;
    }

    public MatchSetScore(int matchId, int teamId, int setNo, int points) {
        this(matchId, teamId, setNo, points, null, null);
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

    public int getSetNo() {
        return setNo;
    }

    public void setSetNo(int setNo) {
        this.setNo = setNo;
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
