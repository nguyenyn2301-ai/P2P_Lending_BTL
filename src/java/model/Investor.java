package model;

import java.math.BigDecimal;

public class Investor {
    private long investorId;
    private String firstName;
    private String lastName;
    private BigDecimal walletBalance;
    private BigDecimal frozenBalance;
    private String riskAppetite;
    private String verificationStatus;
    private String email;

    public Investor() {}

    public long getInvestorId() { return investorId; }
    public void setInvestorId(long investorId) { this.investorId = investorId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public BigDecimal getWalletBalance() { return walletBalance; }
    public void setWalletBalance(BigDecimal walletBalance) { this.walletBalance = walletBalance; }

    public BigDecimal getFrozenBalance() { return frozenBalance; }
    public void setFrozenBalance(BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }

    public String getRiskAppetite() { return riskAppetite; }
    public void setRiskAppetite(String riskAppetite) { this.riskAppetite = riskAppetite; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
