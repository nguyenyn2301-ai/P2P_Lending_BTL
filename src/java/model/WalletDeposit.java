package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class WalletDeposit {
    private long depositId;
    private long investorId;
    private BigDecimal amount;
    private String transferContent;
    private String cicPdfUrl;
    private String status;
    private Timestamp createdAt;
    private String investorName;

    public WalletDeposit() {}

    public long getDepositId() { return depositId; }
    public void setDepositId(long depositId) { this.depositId = depositId; }

    public long getInvestorId() { return investorId; }
    public void setInvestorId(long investorId) { this.investorId = investorId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransferContent() { return transferContent; }
    public void setTransferContent(String transferContent) { this.transferContent = transferContent; }

    public String getCicPdfUrl() { return cicPdfUrl; }
    public void setCicPdfUrl(String cicPdfUrl) { this.cicPdfUrl = cicPdfUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getInvestorName() { return investorName; }
    public void setInvestorName(String investorName) { this.investorName = investorName; }
}
