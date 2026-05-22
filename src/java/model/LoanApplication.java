package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class LoanApplication {
    // 1. CÁC TRƯỜNG DỮ LIỆU CỐT LÕI (Khớp 100% với bảng loan_applications trong DB)
    private long applicationId;
    private long loanId;
    private long borrowerId;
    private BigDecimal amountRequested; // Đổi sang BigDecimal để tránh sai số tiền tệ tài chính
    private int termMonths;
    private String status;
    private Date cicIssuedDate;
    private String cicPdfUrl;
    private Timestamp createdAt;

    // 2. CÁC TRƯỜNG BỔ SUNG KHI JOIN VỚI BẢNG 'loans' VÀ 'borrowers' ĐỂ HIỂN THỊ LÊN SÀN
    private String maskedBorrowerName;  // Tên người vay đã ẩn danh (Ví dụ: "Linh Hà" -> "L**h H*")
    private BigDecimal interestRate;    // Lãi suất chính thức lưu tại bảng loans
    private BigDecimal currentFunded;   // Số tiền hiện tại đã được investor góp vốn (lấy từ loans.current_funded)
    private String loanStatus;          // Trạng thái gọi vốn riêng của bảng loans ('funding', 'success', 'failed')

    // Constructor rỗng mặc định (Bắt buộc phải có để các thư viện hoặc hệ thống mapping hoạt động)
    public LoanApplication() {}

    // Constructor để tạo đơn vay mới từ phía Borrower (Khi điền Form đăng ký vay)
    public LoanApplication(long borrowerId, BigDecimal amountRequested, int termMonths, Date cicIssuedDate, String cicPdfUrl) {
        this.borrowerId = borrowerId;
        this.amountRequested = amountRequested;
        this.termMonths = termMonths;
        this.cicIssuedDate = cicIssuedDate;
        this.cicPdfUrl = cicPdfUrl;
    }

    // Constructor đầy đủ dùng cho hàm DAO khi thực hiện câu lệnh JOIN lấy dữ liệu đẩy lên "Chợ Gọi Vốn"
    public LoanApplication(long applicationId, long borrowerId, BigDecimal amountRequested, int termMonths, 
                           String status, Date cicIssuedDate, String cicPdfUrl, Timestamp createdAt, 
                           String maskedBorrowerName, BigDecimal interestRate, BigDecimal currentFunded, String loanStatus) {
        this.applicationId = applicationId;
        this.borrowerId = borrowerId;
        this.amountRequested = amountRequested;
        this.termMonths = termMonths;
        this.status = status;
        this.cicIssuedDate = cicIssuedDate;
        this.cicPdfUrl = cicPdfUrl;
        this.createdAt = createdAt;
        this.maskedBorrowerName = maskedBorrowerName;
        this.interestRate = interestRate;
        this.currentFunded = currentFunded;
        this.loanStatus = loanStatus;
    }

    // =========================================================================
    // GETTER VÀ SETTER CHO CÁC TRƯỜNG CỐT LÕI
    // =========================================================================
    public long getApplicationId() { return applicationId; }
    public void setApplicationId(long applicationId) { this.applicationId = applicationId; }

    public long getLoanId() { return loanId; }
    public void setLoanId(long loanId) { this.loanId = loanId; }

    public long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(long borrowerId) { this.borrowerId = borrowerId; }

    public BigDecimal getAmountRequested() { return amountRequested; }
    public void setAmountRequested(BigDecimal amountRequested) { this.amountRequested = amountRequested; }

    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCicIssuedDate() { return cicIssuedDate; }
    public void setCicIssuedDate(Date cicIssuedDate) { this.cicIssuedDate = cicIssuedDate; }

    public String getCicPdfUrl() { return cicPdfUrl; }
    public void setCicPdfUrl(String cicPdfUrl) { this.cicPdfUrl = cicPdfUrl; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // =========================================================================
    // GETTER VÀ SETTER CHO CÁC TRƯỜNG HIỂN THỊ SÀN (MỚI TỐI ƯU)
    // =========================================================================
    public String getMaskedBorrowerName() { return maskedBorrowerName; }
    public void setMaskedBorrowerName(String maskedBorrowerName) { this.maskedBorrowerName = maskedBorrowerName; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public BigDecimal getCurrentFunded() { return currentFunded; }
    public void setCurrentFunded(BigDecimal currentFunded) { this.currentFunded = currentFunded; }

    public String getLoanStatus() { return loanStatus; }
    public void setLoanStatus(String loanStatus) { this.loanStatus = loanStatus; }

    /**
     * Hàm bổ trợ tính toán Tiến độ gọi vốn (%) hiển thị trên giao diện (Progress Bar)
     * Tránh lỗi chia cho 0 và tính toán chuẩn xác với BigDecimal
     */
    public double getFundingProgress() {
        if (amountRequested == null || amountRequested.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        if (currentFunded == null) {
            return 0.0;
        }
        // Công thức: (currentFunded / amountRequested) * 100
        return currentFunded.divide(amountRequested, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .doubleValue();
    }
}