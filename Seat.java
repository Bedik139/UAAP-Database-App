public class Seat {

    private int seatId;
    private String seatType;

    public Seat() {}

    public Seat(int seatId, String seatType) {
        this.seatId = seatId;
        this.seatType = seatType;
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

    @Override
    public String toString() {
        return String.format("#%d %s", seatId, seatType);
    }
}
