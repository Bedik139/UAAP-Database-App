import java.math.BigDecimal;

public class Seat {

    private int seatId;
    private String seatType;
    private String venueAddress;
    private String seatStatus;
    private Ticket ticketTier;

    public Seat() {}

    public Seat(int seatId,
                String seatType,
                String venueAddress,
                String seatStatus,
                Ticket ticketTier) {
        this.seatId = seatId;
        this.seatType = seatType;
        this.venueAddress = venueAddress;
        this.seatStatus = seatStatus;
        this.ticketTier = ticketTier;
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

    public Ticket getTicketTier() {
        return ticketTier;
    }

    public void setTicketTier(Ticket ticketTier) {
        this.ticketTier = ticketTier;
    }

    public int getTicketId() {
        return ticketTier != null ? ticketTier.getTicketId() : 0;
    }

    public BigDecimal getTicketPrice() {
        return ticketTier != null ? ticketTier.getEffectivePrice() : null;
    }

    @Override
    public String toString() {
        String venueLabel = venueAddress != null ? " @ " + venueAddress : "";
        String priceLabel = "";
        BigDecimal price = getTicketPrice();
        if (price != null) {
            priceLabel = String.format(" PHP %s", price.stripTrailingZeros().toPlainString());
        }
        return String.format("#%d %s%s [%s]%s", seatId, seatType, venueLabel, seatStatus, priceLabel);
    }
}
