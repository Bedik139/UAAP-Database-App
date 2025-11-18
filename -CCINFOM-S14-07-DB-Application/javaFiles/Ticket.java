import java.math.BigDecimal;

public class Ticket {

    private int ticketId;
    private BigDecimal defaultPrice;
    private BigDecimal price;
    private String status;

    public Ticket() {}

    public Ticket(int ticketId,
                  BigDecimal defaultPrice,
                  BigDecimal price,
                  String status) {
        this.ticketId = ticketId;
        this.defaultPrice = defaultPrice;
        this.price = price;
        this.status = status;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(BigDecimal defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getEffectivePrice() {
        return price != null ? price : defaultPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        BigDecimal effective = getEffectivePrice();
        return String.format("#%d %s (%s)", ticketId, effective, status);
    }
}
