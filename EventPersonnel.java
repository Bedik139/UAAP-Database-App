public class EventPersonnel {

    private int personnelId;
    private String firstName;
    private String lastName;
    private String availabilityStatus;
    private String role;
    private String affiliation;
    private String contactNo;
    private int eventId;
    private Integer matchId;
    private String eventName;
    private String matchLabel;

    public EventPersonnel() {}

    public EventPersonnel(int personnelId,
                          String firstName,
                          String lastName,
                          String availabilityStatus,
                          String role,
                          String affiliation,
                          String contactNo,
                          int eventId,
                          Integer matchId,
                          String eventName,
                          String matchLabel) {
        this.personnelId = personnelId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.availabilityStatus = availabilityStatus;
        this.role = role;
        this.affiliation = affiliation;
        this.contactNo = contactNo;
        this.eventId = eventId;
        this.matchId = matchId;
        this.eventName = eventName;
        this.matchLabel = matchLabel;
    }

    public EventPersonnel(String firstName,
                          String lastName,
                          String availabilityStatus,
                          String role,
                          String affiliation,
                          String contactNo,
                          int eventId,
                          Integer matchId) {
        this(0, firstName, lastName, availabilityStatus, role, affiliation, contactNo,
                eventId, matchId, null, null);
    }

    public int getPersonnelId() {
        return personnelId;
    }

    public void setPersonnelId(int personnelId) {
        this.personnelId = personnelId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Integer getMatchId() {
        return matchId;
    }

    public void setMatchId(Integer matchId) {
        this.matchId = matchId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getMatchLabel() {
        return matchLabel;
    }

    public void setMatchLabel(String matchLabel) {
        this.matchLabel = matchLabel;
    }
}
