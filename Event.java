import java.sql.Date;
import java.sql.Time;

public class Event {
    private int eventId;
    private String eventName;
    private String sport;
    private Date matchDate;
    private Time eventTimeStart;
    private Time eventTimeEnd;
    private String venueAddress;

    // Constructors
    public Event() {}

    public Event(int eventId, String eventName, String sport, Date matchDate,
                 Time eventTimeStart, Time eventTimeEnd, String venueAddress) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.sport = sport;
        this.matchDate = matchDate;
        this.eventTimeStart = eventTimeStart;
        this.eventTimeEnd = eventTimeEnd;
        this.venueAddress = venueAddress;
    }

    public Event(String eventName, String sport, Date matchDate,
                 Time eventTimeStart, Time eventTimeEnd, String venueAddress) {
        this(0, eventName, sport, matchDate, eventTimeStart, eventTimeEnd, venueAddress);
    }

    // Getters / Setters
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public Date getMatchDate() { return matchDate; }
    public void setMatchDate(Date matchDate) { this.matchDate = matchDate; }

    public Time getEventTimeStart() { return eventTimeStart; }
    public void setEventTimeStart(Time eventTimeStart) { this.eventTimeStart = eventTimeStart; }

    public Time getEventTimeEnd() { return eventTimeEnd; }
    public void setEventTimeEnd(Time eventTimeEnd) { this.eventTimeEnd = eventTimeEnd; }

    public String getVenueAddress() { return venueAddress; }
    public void setVenueAddress(String venueAddress) { this.venueAddress = venueAddress; }

    @Override
    public String toString() {
        if (eventId > 0) {
            return String.format("#%d %s (%s)", eventId, eventName, sport);
        }
        return String.format("%s (%s)", eventName, sport);
    }
}
