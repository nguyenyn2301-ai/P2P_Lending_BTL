package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    private long transactionId;
    private long userId;
    private BigDecimal amount;
    private String transactionType;
    private String status;
    private Timestamp createdAt;

    public Transaction() {}

    public long getTransactionId() { return transactionId; }
    public void setTransactionId(long transactionId) { this.transactionId = transactionId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
