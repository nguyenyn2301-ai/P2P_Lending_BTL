package dao; 

import util.DBConnection; 
import model.Loan;
import model.LoanApplication; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    // =========================================================================
    // PHÂN HỆ NGƯỜI ĐI VAY (BORROWER)
    // =========================================================================

    /**
     * Hàm thêm đơn đăng ký vay mới (Nhận đối tượng LoanApplication)
     * Trạng thái khởi tạo mặc định khớp 100% với ENUM trong DB là 'pending'
     */
    public boolean insertLoanApplication(LoanApplication loan) {
        String sql = "INSERT INTO loan_applications (borrower_id, amount_requested, term_months, status, cic_issued_date, cic_pdf_url, created_at) " +
                     "VALUES (?, ?, ?, 'pending', ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, loan.getBorrowerId());
            ps.setBigDecimal(2, loan.getAmountRequested());
            ps.setInt(3, loan.getTermMonths());
            ps.setDate(4, loan.getCicIssuedDate()); 
            ps.setString(5, loan.getCicPdfUrl());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hàm cũ nhận tham số rời (Giữ lại để tránh lỗi biên dịch chéo ở các phân hệ cũ)
     */
    public boolean insertLoanApplication(long borrowerId, double amountRequested, int termMonths, java.sql.Date cicIssuedDate, String cicPdfUrl) {
        String sql = "INSERT INTO loan_applications (borrower_id, amount_requested, term_months, status, cic_issued_date, cic_pdf_url, created_at) " +
                     "VALUES (?, ?, ?, 'pending', ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, borrowerId);
            ps.setBigDecimal(2, BigDecimal.valueOf(amountRequested));
            ps.setInt(3, termMonths);
            ps.setDate(4, cicIssuedDate); 
            ps.setString(5, cicPdfUrl);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy danh sách đơn vay cá nhân hiển thị ở màn hình "Tổng Overview" của người vay
     */
    public List<LoanApplication> getLoansByBorrower(long borrowerId) {
        List<LoanApplication> list = new ArrayList<>();
        String sql = "SELECT application_id, borrower_id, amount_requested, term_months, status, cic_issued_date, cic_pdf_url, created_at " +
                     "FROM loan_applications WHERE borrower_id = ? ORDER BY application_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, borrowerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LoanApplication loan = new LoanApplication();
                    loan.setApplicationId(rs.getLong("application_id"));
                    loan.setBorrowerId(rs.getLong("borrower_id"));
                    loan.setAmountRequested(rs.getBigDecimal("amount_requested"));
                    loan.setTermMonths(rs.getInt("term_months"));
                    
                    // Chuyển đổi hiển thị trạng thái cho thân thiện với giao diện tiếng Việt
                    String dbStatus = rs.getString("status");
                    if ("pending".equalsIgnoreCase(dbStatus)) loan.setStatus("Chờ duyệt");
                    else if ("approved".equalsIgnoreCase(dbStatus)) loan.setStatus("Đã duyệt");
                    else if ("rejected".equalsIgnoreCase(dbStatus)) loan.setStatus("Bị từ chối");
                    else if ("funded".equalsIgnoreCase(dbStatus)) loan.setStatus("Đã gọi vốn xong");
                    else loan.setStatus(dbStatus);

                    loan.setCicIssuedDate(rs.getDate("cic_issued_date"));
                    loan.setCicPdfUrl(rs.getString("cic_pdf_url"));
                    loan.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(loan);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * HOÀN THIỆN CHỢ VAY: Lấy danh sách khoản vay hiển thị công khai trên "Khoản Vay Trên Sàn"
     * Lấy dữ liệu chính từ bảng 'loans' kết hợp JOIN ẩn danh tên người vay
     */
    public List<LoanApplication> getAllMarketLoans() {
        List<LoanApplication> list = new ArrayList<>();
        
        // Thực hiện câu lệnh JOIN 4 bảng để thu thập dữ liệu hiển thị hoàn chỉnh lên sàn công khai
        String sql = "SELECT l.loan_id, app.application_id, app.amount_requested, app.term_months, app.created_at, " +
                     "       CONCAT(b.first_name, ' ', b.last_name) AS full_name, l.interest_rate, l.current_funded, l.status AS loan_status " +
                     "FROM loans l " +
                     "INNER JOIN loan_applications app ON l.application_id = app.application_id " +
                     "INNER JOIN borrowers b ON app.borrower_id = b.borrower_id " +
                     "WHERE l.status = 'funding' ORDER BY l.loan_id DESC";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                LoanApplication loan = new LoanApplication();
                loan.setLoanId(rs.getLong("loan_id"));
                loan.setApplicationId(rs.getLong("application_id"));
                loan.setAmountRequested(rs.getBigDecimal("amount_requested"));
                loan.setTermMonths(rs.getInt("term_months"));
                loan.setCreatedAt(rs.getTimestamp("created_at"));
                
                String fullName = rs.getString("full_name");
                loan.setMaskedBorrowerName(maskName(fullName));
                
                // Lấy các trường thông tin của bảng loans
                loan.setInterestRate(rs.getBigDecimal("interest_rate"));
                loan.setCurrentFunded(rs.getBigDecimal("current_funded"));
                loan.setLoanStatus(rs.getString("loan_status"));
                loan.setStatus("Đang gọi vốn"); 

                // Bảo mật tuyệt đối: Không đổ thông tin định danh và hồ sơ CIC lên sàn công khai
                loan.setBorrowerId(0); 
                loan.setCicIssuedDate(null);
                loan.setCicPdfUrl(null);
                
                list.add(loan);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // PHÂN HỆ ADMIN (BAN QUẢN TRỊ KIỂM DUYỆT)
    // =========================================================================

    /**
     * Lấy danh sách đơn vay đang chờ Admin duyệt ('pending')
     */
    public List<LoanApplication> getPendingLoans() {
        List<LoanApplication> list = new ArrayList<>();
        String sql = "SELECT application_id, borrower_id, amount_requested, term_months, status, cic_issued_date, cic_pdf_url, created_at " +
                     "FROM loan_applications WHERE status = 'pending' ORDER BY created_at ASC"; 
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                LoanApplication loan = new LoanApplication();
                loan.setApplicationId(rs.getLong("application_id"));
                loan.setBorrowerId(rs.getLong("borrower_id"));
                loan.setAmountRequested(rs.getBigDecimal("amount_requested"));
                loan.setTermMonths(rs.getInt("term_months"));
                loan.setStatus("Chờ duyệt");
                loan.setCicIssuedDate(rs.getDate("cic_issued_date"));
                loan.setCicPdfUrl(rs.getString("cic_pdf_url"));
                loan.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(loan);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * HOÀN THIỆN TRANSACTION: Cập nhật trạng thái đơn vay đồng thời tạo Khoản Vay Trên Sàn nếu được duyệt
     * Không chỉnh sửa cấu trúc bảng Database SQL, xử lý logic hoàn toàn bằng cơ chế đồng bộ an toàn của Java.
     * * @param status: truyền vào 'approved' hoặc 'rejected' đúng chuẩn DB
     * @param defaultInterestRate: Mức lãi suất sàn áp dụng khi đơn được duyệt (ví dụ: 12.5)
     */
    public boolean updateLoanStatus(long applicationId, String status, double defaultInterestRate) {
        String sqlUpdateApp = "UPDATE loan_applications SET status = ? WHERE application_id = ?";
        
        // Cải tiến an toàn: Chỉ chèn sang bảng loans nếu đơn đó chưa từng được chèn trước đây (Tránh lỗi nhấn đúp/F5 của Admin)
        String sqlInsertLoan = "INSERT INTO loans (application_id, total_amount, current_funded, interest_rate, status, funding_deadline, updated_at) " +
                              "SELECT application_id, amount_requested, ?, ?, 'funding', DATE_ADD(CURDATE(), INTERVAL 30 DAY), NOW() " +
                              "FROM loan_applications " +
                              "WHERE application_id = ? AND NOT EXISTS (SELECT 1 FROM loans WHERE application_id = ?)";
        
        Connection conn = null;
        PreparedStatement psUpdate = null;
        PreparedStatement psInsert = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu chuỗi Transaction bảo vệ tính nhất quán dữ liệu
            
            // Lệnh 1: Cập nhật trạng thái đơn ứng tuyển (approved/rejected)
            psUpdate = conn.prepareStatement(sqlUpdateApp);
            psUpdate.setString(1, status);
            psUpdate.setLong(2, applicationId);
            int rowsUpdate = psUpdate.executeUpdate();
            
            // Lệnh 2: Nếu trạng thái duyệt là 'approved', tiến hành nhân bản dữ liệu sang bảng 'loans' để lên sàn luôn
            if (rowsUpdate > 0 && "approved".equalsIgnoreCase(status)) {
                psInsert = conn.prepareStatement(sqlInsertLoan);
                
                // Thiết lập các tham số số thực lớn BigDecimal rõ ràng, đồng bộ cấu trúc mới
                psInsert.setBigDecimal(1, BigDecimal.ZERO); // Tiền đã gọi ban đầu = 0.00
                psInsert.setBigDecimal(2, BigDecimal.valueOf(defaultInterestRate)); // Lãi suất quy định
                psInsert.setLong(3, applicationId);
                psInsert.setLong(4, applicationId); // Dùng cho mệnh đề kiểm tra NOT EXISTS chống trùng lặp
                
                psInsert.executeUpdate();
            }
            
            conn.commit(); // Ghi nhận thay đổi thành công xuống Database
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try { 
                    conn.rollback(); // Có bất kỳ lỗi gì phát sinh -> Hủy bỏ toàn bộ thao tác, trả trạng thái ban đầu
                } catch (SQLException ex) { 
                    ex.printStackTrace(); 
                }
            }
            e.printStackTrace();
        } finally {
            // Đóng tài nguyên an toàn theo đúng thứ tự giải phóng bộ nhớ của Hệ thống
            try { if (psUpdate != null) psUpdate.close(); } catch (Exception e) {}
            try { if (psInsert != null) psInsert.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
        return false;
    }

    /**
     * Hàm bổ trợ ẩn danh chuỗi ký tự tên (Hà Phương Linh -> H****** Linh)
     */
    private String maskName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "Ẩn danh";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            
            return parts[0].charAt(0) + "**";
        }
        StringBuilder masked = new StringBuilder(parts[0].charAt(0) + "**");
        for (int i = 1; i < parts.length - 1; i++) {
            masked.append(" ***");
        }
        masked.append(" ").append(parts[parts.length - 1]);
        return masked.toString();
    }

    // =========================================================================
    // PHẦN BỔ SUNG: QUẢN LÝ GÓI VAY, GỌI VỐN, TRẢ NỢ (THÊM MỚI)
    // =========================================================================

    public Loan getLoanById(long loanId) {
        String sql = "SELECT l.*, la.term_months, la.borrower_id, la.interest_rate AS app_rate, "
                + "CONCAT(b.first_name, ' ', b.last_name) AS borrower_name, u.email AS borrower_email "
                + "FROM loans l "
                + "INNER JOIN loan_applications la ON l.application_id = la.application_id "
                + "INNER JOIN borrowers b ON la.borrower_id = b.borrower_id "
                + "INNER JOIN users u ON b.borrower_id = u.user_id "
                + "WHERE l.loan_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapLoanRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addCurrentFunded(long loanId, BigDecimal amount) {
        String sql = "UPDATE loans SET current_funded = current_funded + ? WHERE loan_id = ? "
                + "AND current_funded + ? <= total_amount";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, loanId);
            ps.setBigDecimal(3, amount);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markFullyFunded(long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan == null || !loan.isFullyFunded()) return false;
        String sql = "UPDATE loans SET status = 'process', funding_deadline = NULL WHERE loan_id = ? AND status = 'funding'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean disburseLoan(long loanId) {
        Connection conn = null;
        try {
            Loan loan = getLoanById(loanId);
            if (loan == null) return false;
            if (loan.isFullyFunded() && "funding".equals(loan.getStatus())) {
                markFullyFunded(loanId);
                loan = getLoanById(loanId);
            }
            if (loan == null || !loan.isFullyFunded() || "completed".equals(loan.getStatus())) return false;
            if (!"process".equals(loan.getStatus()) && !"funding".equals(loan.getStatus())) return false;

            BigDecimal total = loan.getTotalAmount();
            BigDecimal fee = total.multiply(new BigDecimal("0.10"));
            BigDecimal disbursed = total.subtract(fee);

            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlLoan = "UPDATE loans SET status = 'process', service_fee = ?, actual_disbursed = ?, "
                    + "due_date = DATE_ADD(CURDATE(), INTERVAL 1 MONTH), paid_periods = 0 WHERE loan_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlLoan)) {
                ps.setBigDecimal(1, fee);
                ps.setBigDecimal(2, disbursed);
                ps.setLong(3, loanId);
                ps.executeUpdate();
            }

            InvestmentDAO invDao = new InvestmentDAO();
            for (model.Investment inv : invDao.getByLoanId(loanId)) {
                String sqlFrozen = "UPDATE investors SET frozen_balance = frozen_balance - ? WHERE investor_id = ? AND frozen_balance >= ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlFrozen)) {
                    ps.setBigDecimal(1, inv.getAmountInvested());
                    ps.setLong(2, inv.getInvestorId());
                    ps.setBigDecimal(3, inv.getAmountInvested());
                    ps.executeUpdate();
                }
            }

            String sqlBorrower = "UPDATE borrowers SET wallet_balance = wallet_balance + ? WHERE borrower_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlBorrower)) {
                ps.setBigDecimal(1, disbursed);
                ps.setLong(2, loan.getBorrowerId());
                ps.executeUpdate();
            }

            TransactionDAO txDao = new TransactionDAO();
            txDao.insertTransaction(loan.getBorrowerId(), disbursed, "disbursement", "completed");
            txDao.insertTransaction(loan.getBorrowerId(), fee, "service_fee_deducted", "completed");

            NotificationDAO notifDao = new NotificationDAO();
            notifDao.addNotification(loan.getBorrowerId(), "Giải ngân thành công",
                    "Gói vay #" + loanId + " đã giải ngân " + disbursed + " VNĐ (đã trừ 10% phí sàn).");

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) {}
        }
        return false;
    }

    public List<Loan> getLoansByStatus(String status) {
        return getLoansFiltered(status, null);
    }

    public List<Loan> getFundingLoansCurrent() {
        String sql = "SELECT l.*, la.term_months, la.borrower_id, la.interest_rate AS app_rate, "
                + "CONCAT(b.first_name, ' ', b.last_name) AS borrower_name, u.email AS borrower_email "
                + "FROM loans l INNER JOIN loan_applications la ON l.application_id = la.application_id "
                + "INNER JOIN borrowers b ON la.borrower_id = b.borrower_id "
                + "INNER JOIN users u ON b.borrower_id = u.user_id "
                + "WHERE l.status = 'funding' AND (l.funding_deadline IS NULL OR l.funding_deadline >= CURDATE()) "
                + "AND l.current_funded < l.total_amount ORDER BY l.loan_id DESC";
        return queryLoanList(sql);
    }

    public List<Loan> getExpiredFundingLoans() {
        String sql = "SELECT l.*, la.term_months, la.borrower_id, la.interest_rate AS app_rate, "
                + "CONCAT(b.first_name, ' ', b.last_name) AS borrower_name, u.email AS borrower_email "
                + "FROM loans l INNER JOIN loan_applications la ON l.application_id = la.application_id "
                + "INNER JOIN borrowers b ON la.borrower_id = b.borrower_id "
                + "INNER JOIN users u ON b.borrower_id = u.user_id "
                + "WHERE l.status IN ('funding','failed') AND l.current_funded < l.total_amount "
                + "AND (l.funding_deadline < CURDATE() OR (l.funding_deadline IS NULL AND l.updated_at < DATE_SUB(CURDATE(), INTERVAL 30 DAY))) "
                + "ORDER BY l.loan_id DESC";
        return queryLoanList(sql);
    }

    public List<Loan> getProcessingLoans() {
        return getLoansFiltered("process", null);
    }

    public List<Loan> getCompletedLoans() {
        return getLoansFiltered("completed", null);
    }

    public List<Loan> getOverdueLoans() {
        return getLoansFiltered("overdue", null);
    }

    public List<Loan> getLoansAwaitingClosure() {
        String sql = "SELECT l.*, la.term_months, la.borrower_id, la.interest_rate AS app_rate, "
                + "CONCAT(b.first_name, ' ', b.last_name) AS borrower_name, u.email AS borrower_email "
                + "FROM loans l INNER JOIN loan_applications la ON l.application_id = la.application_id "
                + "INNER JOIN borrowers b ON la.borrower_id = b.borrower_id "
                + "INNER JOIN users u ON b.borrower_id = u.user_id "
                + "WHERE l.borrower_confirmed = TRUE AND l.status = 'process' ORDER BY l.loan_id DESC";
        return queryLoanList(sql);
    }

    private List<Loan> getLoansFiltered(String status, String extra) {
        String sql = "SELECT l.*, la.term_months, la.borrower_id, la.interest_rate AS app_rate, "
                + "CONCAT(b.first_name, ' ', b.last_name) AS borrower_name, u.email AS borrower_email "
                + "FROM loans l INNER JOIN loan_applications la ON l.application_id = la.application_id "
                + "INNER JOIN borrowers b ON la.borrower_id = b.borrower_id "
                + "INNER JOIN users u ON b.borrower_id = u.user_id "
                + "WHERE l.status = ? ORDER BY l.loan_id DESC";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLoanRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<Loan> queryLoanList(String sql) {
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapLoanRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Loan> getActiveLoansByBorrower(long borrowerId) {
        String sql = "SELECT l.*, la.term_months, la.borrower_id, la.interest_rate AS app_rate, "
                + "CONCAT(b.first_name, ' ', b.last_name) AS borrower_name, u.email AS borrower_email "
                + "FROM loans l INNER JOIN loan_applications la ON l.application_id = la.application_id "
                + "INNER JOIN borrowers b ON la.borrower_id = b.borrower_id "
                + "INNER JOIN users u ON b.borrower_id = u.user_id "
                + "WHERE la.borrower_id = ? AND l.status IN ('process','overdue') ORDER BY l.loan_id DESC";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, borrowerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLoanRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateLoanStatusById(long loanId, String status) {
        String sql = "UPDATE loans SET status = ? WHERE loan_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, loanId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean advanceRepaymentPeriod(long loanId, int newPaidPeriods, java.sql.Date nextDueDate, String newStatus) {
        String sql = "UPDATE loans SET paid_periods = ?, due_date = ?, status = ? WHERE loan_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newPaidPeriods);
            ps.setDate(2, nextDueDate);
            ps.setString(3, newStatus);
            ps.setLong(4, loanId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setFundingDeadlineOnCreate(long applicationId) {
        String sql = "UPDATE loans SET funding_deadline = DATE_ADD(CURDATE(), INTERVAL 30 DAY) WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean approvePendingInvestment(long investmentId) {
        InvestmentDAO invDao = new InvestmentDAO();
        model.Investment inv = invDao.getById(investmentId);
        if (inv == null || !"pending".equals(inv.getStatus())) return false;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            InvestorDAO investorDAO = new InvestorDAO();
            if (!investorDAO.deductWalletAndAddFrozen(inv.getInvestorId(), inv.getAmountInvested())) {
                conn.rollback();
                return false;
            }

            String sqlFund = "UPDATE loans SET current_funded = current_funded + ? WHERE loan_id = ? "
                    + "AND current_funded + ? <= total_amount";
            try (PreparedStatement ps = conn.prepareStatement(sqlFund)) {
                ps.setBigDecimal(1, inv.getAmountInvested());
                ps.setLong(2, inv.getLoanId());
                ps.setBigDecimal(3, inv.getAmountInvested());
                if (ps.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            String sqlInv = "UPDATE investments SET status = 'completed' WHERE investment_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlInv)) {
                ps.setLong(1, investmentId);
                ps.executeUpdate();
            }

            conn.commit();

            Loan loan = getLoanById(inv.getLoanId());
            if (loan != null && loan.isFullyFunded()) {
                markFullyFunded(inv.getLoanId());
                NotificationDAO notif = new NotificationDAO();
                notif.addNotification(loan.getBorrowerId(), "Gọi vốn thành công",
                        "Gói vay #" + inv.getLoanId() + " đã gom đủ 100% vốn, chuyển sang gói đang xử lý.");
            }
            return true;
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) {}
        }
        return false;
    }

    public boolean markLoanAwaitingClosure(long loanId) {
        return updateLoanStatusById(loanId, "completed");
    }

    public boolean closeLoanPackage(long loanId) {
        return updateLoanStatusById(loanId, "completed");
    }

    private Loan mapLoanRow(ResultSet rs) throws SQLException {
        Loan l = new Loan();
        l.setLoanId(rs.getLong("loan_id"));
        l.setApplicationId(rs.getLong("application_id"));
        l.setLoanCode(rs.getString("loan_code"));
        l.setTotalAmount(rs.getBigDecimal("total_amount"));
        l.setCurrentFunded(rs.getBigDecimal("current_funded"));
        l.setInterestRate(rs.getBigDecimal("interest_rate"));
        l.setServiceFee(rs.getBigDecimal("service_fee"));
        l.setActualDisbursed(rs.getBigDecimal("actual_disbursed"));
        l.setStatus(rs.getString("status"));
        l.setBorrowerConfirmed(rs.getBoolean("borrower_confirmed"));
        l.setInvestorConfirmed(rs.getBoolean("investor_confirmed"));
        try { l.setDueDate(rs.getDate("due_date")); } catch (SQLException ignored) {}
        try { l.setPaidPeriods(rs.getInt("paid_periods")); } catch (SQLException ignored) {}
        try { l.setTermMonths(rs.getInt("term_months")); } catch (SQLException ignored) {}
        try { l.setBorrowerId(rs.getLong("borrower_id")); } catch (SQLException ignored) {}
        try { l.setBorrowerName(rs.getString("borrower_name")); } catch (SQLException ignored) {}
        try { l.setBorrowerEmail(rs.getString("borrower_email")); } catch (SQLException ignored) {}
        l.setUpdatedAt(rs.getTimestamp("updated_at"));
        return l;
    }
}