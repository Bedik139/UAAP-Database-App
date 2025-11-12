import java.math.BigDecimal;
import java.sql.Timestamp;

public class SeatAndTicket {

    private int recordId;
    private int seatId;
    private int eventId;
    private int customerId;
    private Timestamp saleDatetime;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private int ticketId;
    private Integer matchId;
    private String saleStatus;

    private String seatLabel;
    private String eventName;
    private String customerName;
    private String ticketLabel;
    private String matchLabel;

    public SeatAndTicket() {}

    public SeatAndTicket(int recordId,
                         int seatId,
                         int eventId,
                         int customerId,
                         Timestamp saleDatetime,
                         int quantity,
                         BigDecimal unitPrice,
                         BigDecimal totalPrice,
                         int ticketId,
                         Integer matchId,
                         String saleStatus,
                         String seatLabel,
                         String eventName,
                         String customerName,
                         String ticketLabel,
                         String matchLabel) {
        this.recordId = recordId;
        this.seatId = seatId;
        this.eventId = eventId;
        this.customerId = customerId;
        this.saleDatetime = saleDatetime;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.ticketId = ticketId;
        this.matchId = matchId;
        this.saleStatus = saleStatus;
        this.seatLabel = seatLabel;
        this.eventName = eventName;
        this.customerName = customerName;
        this.ticketLabel = ticketLabel;
        this.matchLabel = matchLabel;
    }

    public SeatAndTicket(int seatId,
                         int eventId,
                         int customerId,
                         Timestamp saleDatetime,
                         int quantity,
                         BigDecimal unitPrice,
                         int ticketId,
                         Integer matchId,
                         String saleStatus) {
        this(0, seatId, eventId, customerId, saleDatetime, quantity, unitPrice,
                unitPrice.multiply(BigDecimal.valueOf(quantity)),
                ticketId, matchId,
                saleStatus, null, null, null, null, null);
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Timestamp getSaleDatetime() {
        return saleDatetime;
    }

    public void setSaleDatetime(Timestamp saleDatetime) {
        this.saleDatetime = saleDatetime;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public Integer getMatchId() {
        return matchId;
    }

    public void setMatchId(Integer matchId) {
        this.matchId = matchId;
    }

    public String getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(String saleStatus) {
        this.saleStatus = saleStatus;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getTicketLabel() {
        return ticketLabel;
    }

    public void setTicketLabel(String ticketLabel) {
        this.ticketLabel = ticketLabel;
    }

    public String getMatchLabel() {
        return matchLabel;
    }

    public void setMatchLabel(String matchLabel) {
        this.matchLabel = matchLabel;
    }

    public boolean isSold() {
        return "Sold".equalsIgnoreCase(saleStatus);
    }
}
