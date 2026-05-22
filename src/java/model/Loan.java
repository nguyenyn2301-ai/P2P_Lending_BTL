package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Loan {
    private long loanId;
    private long applicationId;
    private String loanCode;
    private BigDecimal totalAmount;
    private BigDecimal currentFunded;
    private BigDecimal interestRate;
    private BigDecimal serviceFee;
    private BigDecimal actualDisbursed;
    private String status;
    private boolean borrowerConfirmed;
    private boolean investorConfirmed;
    private Date dueDate;
    private Timestamp updatedAt;
    private int paidPeriods;
    private int termMonths;
    private long borrowerId;
    private String borrowerName;
    private String borrowerEmail;

    public Loan() {}

    public long getLoanId() { return loanId; }
    public void setLoanId(long loanId) { this.loanId = loanId; }

    public long getApplicationId() { return applicationId; }
    public void setApplicationId(long applicationId) { this.applicationId = applicationId; }

    public String getLoanCode() { return loanCode; }
    public void setLoanCode(String loanCode) { this.loanCode = loanCode; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getCurrentFunded() { return currentFunded; }
    public void setCurrentFunded(BigDecimal currentFunded) { this.currentFunded = currentFunded; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }

    public BigDecimal getActualDisbursed() { return actualDisbursed; }
    public void setActualDisbursed(BigDecimal actualDisbursed) { this.actualDisbursed = actualDisbursed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isBorrowerConfirmed() { return borrowerConfirmed; }
    public void setBorrowerConfirmed(boolean borrowerConfirmed) { this.borrowerConfirmed = borrowerConfirmed; }

    public boolean isInvestorConfirmed() { return investorConfirmed; }
    public void setInvestorConfirmed(boolean investorConfirmed) { this.investorConfirmed = investorConfirmed; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public int getPaidPeriods() { return paidPeriods; }
    public void setPaidPeriods(int paidPeriods) { this.paidPeriods = paidPeriods; }

    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }

    public long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(long borrowerId) { this.borrowerId = borrowerId; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public String getBorrowerEmail() { return borrowerEmail; }
    public void setBorrowerEmail(String borrowerEmail) { this.borrowerEmail = borrowerEmail; }

    public double getFundingProgress() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        if (currentFunded == null) return 0.0;
        return currentFunded.divide(totalAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).doubleValue();
    }

    public boolean isFullyFunded() {
        return totalAmount != null && currentFunded != null
                && currentFunded.compareTo(totalAmount) >= 0;
    }
}
