import java.sql.Date;
import java.sql.Time;

public class Event {
    private int eventId;
    private String eventName;
    private String sport;
    private Date eventDate;
    private Time eventTimeStart;
    private Time eventTimeEnd;
    private String venueAddress;
    private String eventStatus;
    private int venueCapacity;

    public Event() {}

    public Event(int eventId,
                 String eventName,
                 String sport,
                 Date eventDate,
                 Time eventTimeStart,
                 Time eventTimeEnd,
                 String venueAddress,
                 String eventStatus,
                 int venueCapacity) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.sport = sport;
        this.eventDate = eventDate;
        this.eventTimeStart = eventTimeStart;
        this.eventTimeEnd = eventTimeEnd;
        this.venueAddress = venueAddress;
        this.eventStatus = eventStatus;
        this.venueCapacity = venueCapacity;
    }

    public Event(String eventName,
                 String sport,
                 Date eventDate,
                 Time eventTimeStart,
                 Time eventTimeEnd,
                 String venueAddress,
                 String eventStatus,
                 int venueCapacity) {
        this(0, eventName, sport, eventDate, eventTimeStart, eventTimeEnd, venueAddress, eventStatus, venueCapacity);
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

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Time getEventTimeStart() {
        return eventTimeStart;
    }

    public void setEventTimeStart(Time eventTimeStart) {
        this.eventTimeStart = eventTimeStart;
    }

    public Time getEventTimeEnd() {
        return eventTimeEnd;
    }

    public void setEventTimeEnd(Time eventTimeEnd) {
        this.eventTimeEnd = eventTimeEnd;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public int getVenueCapacity() {
        return venueCapacity;
    }

    public void setVenueCapacity(int venueCapacity) {
        this.venueCapacity = venueCapacity;
    }

    @Override
    public String toString() {
        String statusLabel = eventStatus != null ? " - " + eventStatus : "";
        if (eventId > 0) {
            return String.format("#%d %s (%s%s)", eventId, eventName, sport, statusLabel);
        }
        return String.format("%s (%s%s)", eventName, sport, statusLabel);
    }
}
