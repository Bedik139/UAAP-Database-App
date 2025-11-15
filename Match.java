import java.sql.Time;
import java.sql.Date;

public class Match {

    private int matchId;
    private int eventId;
    private String eventName;
    private String matchType;
    private Time matchTimeStart;
    private Time matchTimeEnd;
    private String status;
    private String scoreSummary;
    private Date eventDate;

    public Match() {}

    public Match(int matchId,
                 int eventId,
                 String eventName,
                 String matchType,
                 Time matchTimeStart,
                 Time matchTimeEnd,
                 String status,
                 String scoreSummary,
                 Date eventDate) {
        this.matchId = matchId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.matchType = matchType;
        this.matchTimeStart = matchTimeStart;
        this.matchTimeEnd = matchTimeEnd;
        this.status = status;
        this.scoreSummary = scoreSummary;
        this.eventDate = eventDate;
    }

    public Match(int eventId,
                 String matchType,
                 Time matchTimeStart,
                 Time matchTimeEnd,
                 String status) {
        this(0, eventId, null, matchType, matchTimeStart, matchTimeEnd, status, null, null);
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

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    @Override
    public String toString() {
        String label = eventName != null ? eventName : "Match";
        if (matchId > 0) {
            return String.format("#%d - %s (%s)", matchId, label, matchType);
        }
        return String.format("%s (%s)", label, matchType);
    }
}
