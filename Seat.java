public class Seat {

    private int seatId;
    private String seatType;
    private String seatSection;
    private String venueAddress;
    private String seatStatus;

    public Seat() {}

    public Seat(int seatId,
                String seatType,
                String seatSection,
                String venueAddress,
                String seatStatus) {
        this.seatId = seatId;
        this.seatType = seatType;
        this.seatSection = seatSection;
        this.venueAddress = venueAddress;
        this.seatStatus = seatStatus;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getSeatSection() {
        return seatSection;
    }

    public void setSeatSection(String seatSection) {
        this.seatSection = seatSection;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getSeatStatus() {
        return seatStatus;
    }

    public void setSeatStatus(String seatStatus) {
        this.seatStatus = seatStatus;
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(seatStatus);
    }

    @Override
    public String toString() {
        String sectionLabel = seatSection != null && !seatSection.isEmpty()
                ? " " + seatSection
                : "";
        String venueLabel = venueAddress != null ? " @ " + venueAddress : "";
        return String.format("#%d %s%s%s [%s]", seatId, seatType, sectionLabel, venueLabel, seatStatus);
    }
}
