package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Investment {
    private long investmentId;
    private long loanId;
    private long investorId;
    private BigDecimal amountInvested;
    private String status;
    private String sourceBankName;
    private String sourceAccountNumber;
    private String receiptUrl;
    private Timestamp investedAt;
    private String investorName;
    private String loanCode;
    private BigDecimal loanTotalAmount;

    public Investment() {}

    public long getInvestmentId() { return investmentId; }
    public void setInvestmentId(long investmentId) { this.investmentId = investmentId; }

    public long getLoanId() { return loanId; }
    public void setLoanId(long loanId) { this.loanId = loanId; }

    public long getInvestorId() { return investorId; }
    public void setInvestorId(long investorId) { this.investorId = investorId; }

    public BigDecimal getAmountInvested() { return amountInvested; }
    public void setAmountInvested(BigDecimal amountInvested) { this.amountInvested = amountInvested; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSourceBankName() { return sourceBankName; }
    public void setSourceBankName(String sourceBankName) { this.sourceBankName = sourceBankName; }

    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public Timestamp getInvestedAt() { return investedAt; }
    public void setInvestedAt(Timestamp investedAt) { this.investedAt = investedAt; }

    public String getInvestorName() { return investorName; }
    public void setInvestorName(String investorName) { this.investorName = investorName; }

    public String getLoanCode() { return loanCode; }
    public void setLoanCode(String loanCode) { this.loanCode = loanCode; }

    public BigDecimal getLoanTotalAmount() { return loanTotalAmount; }
    public void setLoanTotalAmount(BigDecimal loanTotalAmount) { this.loanTotalAmount = loanTotalAmount; }
}
