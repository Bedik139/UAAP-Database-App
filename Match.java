import java.sql.Date;
import java.sql.Time;

public class Match {

    private int matchId;
    private int eventId;
    private String eventName;
    private String matchType;
    private Date matchDate;
    private Time matchTimeStart;
    private Time matchTimeEnd;
    private String status;
    private String scoreSummary;

    public Match() {}

    public Match(int matchId,
                 int eventId,
                 String eventName,
                 String matchType,
                 Date matchDate,
                 Time matchTimeStart,
                 Time matchTimeEnd,
                 String status,
                 String scoreSummary) {
        this.matchId = matchId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.matchType = matchType;
        this.matchDate = matchDate;
        this.matchTimeStart = matchTimeStart;
        this.matchTimeEnd = matchTimeEnd;
        this.status = status;
        this.scoreSummary = scoreSummary;
    }

    public Match(int eventId,
                 String matchType,
                 Date matchDate,
                 Time matchTimeStart,
                 Time matchTimeEnd,
                 String status) {
        this(0, eventId, null, matchType, matchDate, matchTimeStart, matchTimeEnd, status, null);
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public Date getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(Date matchDate) {
        this.matchDate = matchDate;
    }

    public Time getMatchTimeStart() {
        return matchTimeStart;
    }

    public void setMatchTimeStart(Time matchTimeStart) {
        this.matchTimeStart = matchTimeStart;
    }

    public Time getMatchTimeEnd() {
        return matchTimeEnd;
    }

    public void setMatchTimeEnd(Time matchTimeEnd) {
        this.matchTimeEnd = matchTimeEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScoreSummary() {
        return scoreSummary;
    }

    public void setScoreSummary(String scoreSummary) {
        this.scoreSummary = scoreSummary;
    }

    @Override
    public String toString() {
        String label = eventName != null ? eventName : "Match";
        String statusLabel = status != null ? " - " + status : "";
        if (matchId > 0) {
            return String.format("#%d %s (%s%s)", matchId, label, matchType, statusLabel);
        }
        return String.format("%s (%s%s)", label, matchType, statusLabel);
    }
}
