import java.math.BigDecimal;
import java.sql.Timestamp;

public class RefundAudit {
    private int auditId;
    private int seatAndTicketRecId;
    private BigDecimal refundAmount;
    private Timestamp refundDatetime;
    private String reason;
    private String processedBy;

    public RefundAudit() {
    }

    public RefundAudit(int auditId, int seatAndTicketRecId, BigDecimal refundAmount,
                       Timestamp refundDatetime, String reason, String processedBy) {
        this.auditId = auditId;
        this.seatAndTicketRecId = seatAndTicketRecId;
        this.refundAmount = refundAmount;
        this.refundDatetime = refundDatetime;
        this.reason = reason;
        this.processedBy = processedBy;
    }

    public int getAuditId() {
        return auditId;
    }

    public void setAuditId(int auditId) {
        this.auditId = auditId;
    }

    public int getSeatAndTicketRecId() {
        return seatAndTicketRecId;
    }

    public void setSeatAndTicketRecId(int seatAndTicketRecId) {
        this.seatAndTicketRecId = seatAndTicketRecId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Timestamp getRefundDatetime() {
        return refundDatetime;
    }

    public void setRefundDatetime(Timestamp refundDatetime) {
        this.refundDatetime = refundDatetime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }
}
