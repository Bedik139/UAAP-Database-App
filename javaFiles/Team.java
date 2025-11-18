public class Team {

    private int teamId;
    private String teamName;
    private String gender;
    private String sport;
    private int seasonsPlayed;
    private int standingWins;
    private int standingLosses;
    private int totalGamesPlayed;

    public Team() {}

    public Team(int teamId,
                String teamName,
                String gender,
                String sport,
                int seasonsPlayed,
                int standingWins,
                int standingLosses,
                int totalGamesPlayed) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.gender = gender;
        this.sport = sport;
        this.seasonsPlayed = seasonsPlayed;
        this.standingWins = standingWins;
        this.standingLosses = standingLosses;
        this.totalGamesPlayed = totalGamesPlayed;
    }

    public Team(String teamName,
                String gender,
                String sport,
                int seasonsPlayed,
                int standingWins,
                int standingLosses,
                int totalGamesPlayed) {
        this(0, teamName, gender, sport, seasonsPlayed, standingWins, standingLosses, totalGamesPlayed);
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public int getSeasonsPlayed() {
        return seasonsPlayed;
    }

    public void setSeasonsPlayed(int seasonsPlayed) {
        this.seasonsPlayed = seasonsPlayed;
    }

    public int getStandingWins() {
        return standingWins;
    }

    public void setStandingWins(int standingWins) {
        this.standingWins = standingWins;
    }

    public int getStandingLosses() {
        return standingLosses;
    }

    public void setStandingLosses(int standingLosses) {
        this.standingLosses = standingLosses;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public void setTotalGamesPlayed(int totalGamesPlayed) {
        this.totalGamesPlayed = totalGamesPlayed;
    }

    @Override
    public String toString() {
        return teamName;
    }
}
