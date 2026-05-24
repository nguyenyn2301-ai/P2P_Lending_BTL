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
     */
    public boolean insertLoanApplication(LoanApplication loan) {
        String sql = "INSERT INTO loan_applications (borrower_id, amount_requested, term_months, interest_rate, status, cic_issued_date, cic_pdf_url, created_at) " +
                     "VALUES (?, ?, ?, ?, 'pending', ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, loan.getBorrowerId());
            ps.setBigDecimal(2, loan.getAmountRequested());
            ps.setInt(3, loan.getTermMonths());
            ps.setBigDecimal(4, BigDecimal.valueOf(12.0));
            ps.setDate(5, loan.getCicIssuedDate()); 
            ps.setString(6, loan.getCicPdfUrl());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Hàm cũ nhận tham số rời (Giữ lại để tránh lỗi biên dịch chéo ở các phân hệ cũ)
     */
    public boolean insertLoanApplication(long borrowerId, double amountRequested, int termMonths, java.sql.Date cicIssuedDate, String cicPdfUrl) {
        String sql = "INSERT INTO loan_applications (borrower_id, amount_requested, term_months, interest_rate, status, cic_issued_date, cic_pdf_url, created_at) " +
                     "VALUES (?, ?, ?, ?, 'pending', ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, borrowerId);
            ps.setBigDecimal(2, BigDecimal.valueOf(amountRequested));
            ps.setInt(3, termMonths);
            ps.setBigDecimal(4, BigDecimal.valueOf(12.0));
            ps.setDate(5, cicIssuedDate); 
            ps.setString(6, cicPdfUrl);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * HOÀN THIỆN CHỢ VAY: Lấy danh sách khoản vay hiển thị công khai trên "Khoản Vay Trên Sàn"
     */
    public List<LoanApplication> getAllMarketLoans() {
        List<LoanApplication> list = new ArrayList<>();
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
                
                loan.setInterestRate(rs.getBigDecimal("interest_rate"));
                loan.setCurrentFunded(rs.getBigDecimal("current_funded"));
                loan.setLoanStatus(rs.getString("loan_status"));
                loan.setStatus("Đang gọi vốn"); 

                // Bảo mật: Xóa vết dữ liệu nhạy cảm trước khi đưa lên sàn public
                loan.setBorrowerId(0); 
                loan.setCicIssuedDate(null);
                loan.setCicPdfUrl(null);
                
                list.add(loan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Chi tiết gói đang gọi vốn cho nhà đầu tư (có file PDF CIC từ hồ sơ vay).
     */
    public LoanApplication getFundingLoanDetailForInvestor(long loanId) {
        String sql = "SELECT l.loan_id, app.application_id, app.amount_requested, app.term_months, app.created_at, "
                + "app.cic_pdf_url, app.cic_issued_date, "
                + "CONCAT(b.first_name, ' ', b.last_name) AS full_name, l.interest_rate, l.current_funded, l.status AS loan_status "
                + "FROM loans l "
                + "INNER JOIN loan_applications app ON l.application_id = app.application_id "
                + "INNER JOIN borrowers b ON app.borrower_id = b.borrower_id "
                + "WHERE l.loan_id = ? AND l.status = 'funding'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LoanApplication loan = new LoanApplication();
                    loan.setLoanId(rs.getLong("loan_id"));
                    loan.setApplicationId(rs.getLong("application_id"));
                    loan.setAmountRequested(rs.getBigDecimal("amount_requested"));
                    loan.setTermMonths(rs.getInt("term_months"));
                    loan.setCreatedAt(rs.getTimestamp("created_at"));
                    loan.setCicIssuedDate(rs.getDate("cic_issued_date"));
                    loan.setCicPdfUrl(rs.getString("cic_pdf_url"));
                    loan.setMaskedBorrowerName(maskName(rs.getString("full_name")));
                    loan.setInterestRate(rs.getBigDecimal("interest_rate"));
                    loan.setCurrentFunded(rs.getBigDecimal("current_funded"));
                    loan.setLoanStatus(rs.getString("loan_status"));
                    loan.setStatus("Đang gọi vốn");
                    return loan;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Chi tiết gói vốn cho nhà đầu tư (mọi trạng thái loan) — dùng khi xem PDF từ lịch sử góp vốn.
     */
    public LoanApplication getLoanDetailForInvestor(long loanId) {
        String sql = "SELECT l.loan_id, app.application_id, app.amount_requested, app.term_months, app.created_at, "
                + "app.cic_pdf_url, app.cic_issued_date, "
                + "CONCAT(b.first_name, ' ', b.last_name) AS full_name, l.interest_rate, l.current_funded, l.status AS loan_status "
                + "FROM loans l "
                + "INNER JOIN loan_applications app ON l.application_id = app.application_id "
                + "INNER JOIN borrowers b ON app.borrower_id = b.borrower_id "
                + "WHERE l.loan_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LoanApplication loan = new LoanApplication();
                    loan.setLoanId(rs.getLong("loan_id"));
                    loan.setApplicationId(rs.getLong("application_id"));
                    loan.setAmountRequested(rs.getBigDecimal("amount_requested"));
                    loan.setTermMonths(rs.getInt("term_months"));
                    loan.setCreatedAt(rs.getTimestamp("created_at"));
                    loan.setCicIssuedDate(rs.getDate("cic_issued_date"));
                    loan.setCicPdfUrl(rs.getString("cic_pdf_url"));
                    loan.setMaskedBorrowerName(maskName(rs.getString("full_name")));
                    loan.setInterestRate(rs.getBigDecimal("interest_rate"));
                    loan.setCurrentFunded(rs.getBigDecimal("current_funded"));
                    loan.setLoanStatus(rs.getString("loan_status"));
                    loan.setStatus(rs.getString("loan_status"));
                    return loan;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================================
    // PHÂN HỆ ADMIN (BAN QUẢN TRỊ KIỂM DUYỆT)
    // =========================================================================

    /**
     * Lấy danh sách đơn vay đang chờ Admin duyệt ('pending')
     */
    public List<java.util.Map<String, Object>> getLoanPdfDocumentsByBorrower(long borrowerId) {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT application_id, amount_requested, status, cic_pdf_url, created_at "
                + "FROM loan_applications WHERE borrower_id = ? AND cic_pdf_url IS NOT NULL "
                + "ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, borrowerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("applicationId", rs.getLong("application_id"));
                    m.put("amountRequested", rs.getBigDecimal("amount_requested"));
                    m.put("status", rs.getString("status"));
                    m.put("fileUrl", rs.getString("cic_pdf_url"));
                    m.put("createdAt", rs.getTimestamp("created_at"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<LoanApplication> getPendingLoans() {
        List<LoanApplication> list = new ArrayList<>();
        String sql = "SELECT application_id, borrower_id, amount_requested, term_months, status, cic_issued_date, cic_pdf_url, created_at " +
                     "FROM loan_applications WHERE LOWER(TRIM(status)) = 'pending' ORDER BY created_at ASC"; 
                     
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật trạng thái đơn vay đồng thời tạo Khoản Vay Trên Sàn bằng Transaction an toàn.
     */
    public boolean updateLoanStatus(long applicationId, String status, double defaultInterestRate) {
        String sqlUpdateApp = "UPDATE loan_applications SET status = ? WHERE application_id = ? AND status = 'pending'";
        String sqlInsertLoan = "INSERT INTO loans (application_id, total_amount, current_funded, interest_rate, status, funding_deadline, updated_at) " +
                               "SELECT application_id, amount_requested, 0, ?, 'funding', DATE_ADD(CURDATE(), INTERVAL 30 DAY), NOW() " +
                               "FROM loan_applications " +
                               "WHERE application_id = ? AND NOT EXISTS (SELECT 1 FROM loans WHERE application_id = ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateApp)) {
                psUpdate.setString(1, status);
                psUpdate.setLong(2, applicationId);
                int rowsUpdate = psUpdate.executeUpdate();
                
                // Nếu không cập nhật được dòng nào (có thể đơn đã được duyệt trước đó), thực hiện rollback tránh xung đột
                if (rowsUpdate == 0) {
                    conn.rollback();
                    return false;
                }
                
                if ("approved".equalsIgnoreCase(status)) {
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertLoan)) {
                        psInsert.setBigDecimal(1, BigDecimal.valueOf(defaultInterestRate));
                        psInsert.setLong(2, applicationId);
                        psInsert.setLong(3, applicationId);
                        psInsert.executeUpdate();
                    }
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Ẩn danh chuỗi ký tự tên (Hà Phương Linh -> H****** Linh)
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
    // MANAGEMENT SYSTEM (GỌI VỐN, GIẢI NGÂN, TRẢ NỢ)
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addCurrentFunded(long loanId, BigDecimal amount) {
        String sql = "UPDATE loans SET current_funded = current_funded + ? WHERE loan_id = ? "
                + "AND current_funded + ? <= total_amount AND status = 'funding'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, loanId);
            ps.setBigDecimal(3, amount);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markFullyFunded(long loanId) {
        String sql = "UPDATE loans SET status = 'process', funding_deadline = NULL WHERE loan_id = ? AND status = 'funding' AND current_funded = total_amount";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * TỐI ƯU TRANSACTION CHUẨN: Xử lý đóng gói tất cả các thao tác cập nhật số dư,
     * khấu trừ ví đóng băng của Nhà đầu tư trên cùng một Connection để đảm bảo an toàn tài chính.
     */
    public boolean disburseLoan(long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan == null || "completed".equals(loan.getStatus())) return false;
        
        // Ép trạng thái về chuẩn nếu đã gom đủ tiền nhưng chưa kích hoạt trạng thái xử lý
        if (loan.isFullyFunded() && "funding".equals(loan.getStatus())) {
            markFullyFunded(loanId);
            loan = getLoanById(loanId);
        }
        
        if (loan == null || !loan.isFullyFunded() || !"process".equals(loan.getStatus())) return false;

        BigDecimal total = loan.getTotalAmount();
        BigDecimal fee = total.multiply(new BigDecimal("0.10")); // 10% phí nền tảng
        BigDecimal disbursed = total.subtract(fee);

        String sqlLoan = "UPDATE loans SET service_fee = ?, actual_disbursed = ?, "
                       + "due_date = DATE_ADD(CURDATE(), INTERVAL 1 MONTH), paid_periods = 0 WHERE loan_id = ? AND actual_disbursed IS NULL";
        String sqlUpdateAppStatus = "UPDATE loan_applications SET status = 'funded' WHERE application_id = ?";
        String sqlInvestorFrozen = "UPDATE investors SET frozen_balance = frozen_balance - ? WHERE investor_id = ? AND frozen_balance >= ?";
        String sqlBorrowerWallet = "UPDATE borrowers SET wallet_balance = wallet_balance + ? WHERE borrower_id = ?";
        
        String sqlTx = "INSERT INTO transactions (user_id, amount, transaction_type, status, created_at) VALUES (?, ?, ?, 'completed', NOW())";
        String sqlNotif = "INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, FALSE, NOW())";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Cập nhật bảng loans công bố số tiền thực tế giải ngân
                try (PreparedStatement ps = conn.prepareStatement(sqlLoan)) {
                    ps.setBigDecimal(1, fee);
                    ps.setBigDecimal(2, disbursed);
                    ps.setLong(3, loanId);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                // 2. Đồng bộ trạng thái bảng loan_applications sang 'funded'
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateAppStatus)) {
                    ps.setLong(1, loan.getApplicationId());
                    ps.executeUpdate();
                }

                // 3. Trừ quỹ đóng băng (frozen_balance) của toàn bộ các nhà đầu tư tham gia gói vay này
                // Thực hiện truy vấn inline thay vì khởi tạo đối tượng DAO khác làm rò rỉ Connection
                String sqlGetInvestors = "SELECT investor_id, amount_invested FROM investments WHERE loan_id = ? AND status = 'completed'";
                try (PreparedStatement psGet = conn.prepareStatement(sqlGetInvestors)) {
                    psGet.setLong(1, loanId);
                    try (ResultSet rsInv = psGet.executeQuery()) {
                        try (PreparedStatement psSubInvestor = conn.prepareStatement(sqlInvestorFrozen)) {
                            while (rsInv.next()) {
                                long invId = rsInv.getLong("investor_id");
                                BigDecimal amt = rsInv.getBigDecimal("amount_invested");
                                
                                psSubInvestor.setBigDecimal(1, amt);
                                psSubInvestor.setLong(2, invId);
                                psSubInvestor.setBigDecimal(3, amt);
                                psSubInvestor.addBatch();
                            }
                            psSubInvestor.executeBatch();
                        }
                    }
                }

                // 4. Cộng tiền thực nhận vào ví người đi vay (Borrower)
                try (PreparedStatement psB = conn.prepareStatement(sqlBorrowerWallet)) {
                    psB.setBigDecimal(1, disbursed);
                    psB.setLong(2, loan.getBorrowerId());
                    psB.executeUpdate();
                }

                // 5. Ghi lịch sử giao dịch (Transactions log) công khai tài chính
                try (PreparedStatement psTx = conn.prepareStatement(sqlTx)) {
                    // Lịch sử nhận tiền giải ngân
                    psTx.setLong(1, loan.getBorrowerId());
                    psTx.setBigDecimal(2, disbursed);
                    psTx.setString(3, "disbursement");
                    psTx.addBatch();
                    
                    // Lịch sử trừ phí sàn
                    psTx.setLong(1, loan.getBorrowerId());
                    psTx.setBigDecimal(2, fee);
                    psTx.setString(3, "service_fee_deducted");
                    psTx.addBatch();
                    
                    psTx.executeBatch();
                }

                // 6. Gửi thông báo hệ thống (Notification) tới app người dùng
                try (PreparedStatement psNotif = conn.prepareStatement(sqlNotif)) {
                    psNotif.setLong(1, loan.getBorrowerId());
                    psNotif.setString(2, "Giải ngân thành công");
                    psNotif.setString(3, "Gói vay #" + loanId + " đã giải ngân " + disbursed + " VNĐ (đã trừ 10% phí sàn).");
                    psNotif.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                while (rs.next()) {
                    list.add(mapLoanRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<Loan> queryLoanList(String sql) {
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapLoanRow(rs));
            }
        } catch (SQLException e) {
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
                while (rs.next()) {
                    list.add(mapLoanRow(rs));
                }
            }
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * DUYỆT ĐẦU TƯ TRANSACTION: Đảm bảo kiểm tra và trừ tiền đầu tư khép kín, 
     * không gọi các DAO ngoài gây thất thoát, sai lệch số dư ví khi gặp lỗi runtime.
     */
    public boolean approvePendingInvestment(long investmentId) {
        String sqlGetInv = "SELECT investor_id, loan_id, amount_invested, status FROM investments WHERE investment_id = ?";
        String sqlDeductInvestor = "UPDATE investors SET wallet_balance = wallet_balance - ?, frozen_balance = frozen_balance + ? "
                                 + "WHERE investor_id = ? AND wallet_balance >= ?";
        String sqlAddFund = "UPDATE loans SET current_funded = current_funded + ? WHERE loan_id = ? "
                          + "AND current_funded + ? <= total_amount AND status = 'funding'";
        String sqlUpdateInvStatus = "UPDATE investments SET status = 'completed' WHERE investment_id = ?";
        String sqlNotif = "INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, FALSE, NOW())";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long investorId = 0, loanId = 0;
                BigDecimal amt = BigDecimal.ZERO;
                String currentStatus = "";

                // 1. Kiểm tra trạng thái khoản đầu tư trực tiếp
                try (PreparedStatement ps = conn.prepareStatement(sqlGetInv)) {
                    ps.setLong(1, investmentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            investorId = rs.getLong("investor_id");
                            loanId = rs.getLong("loan_id");
                            amt = rs.getBigDecimal("amount_invested");
                            currentStatus = rs.getString("status");
                        }
                    }
                }

                if (!"pending".equals(currentStatus)) {
                    conn.rollback();
                    return false;
                }

                // 2. Chuyển tiền từ ví khả dụng sang ví đóng băng của nhà đầu tư
                try (PreparedStatement psInv = conn.prepareStatement(sqlDeductInvestor)) {
                    psInv.setBigDecimal(1, amt);
                    psInv.setBigDecimal(2, amt);
                    psInv.setLong(3, investorId);
                    psInv.setBigDecimal(4, amt);
                    if (psInv.executeUpdate() == 0) {
                        conn.rollback(); // Hết tiền khả dụng trong ví
                        return false;
                    }
                }

                // 3. Đẩy số tiền gom được vào bảng loans
                try (PreparedStatement psFund = conn.prepareStatement(sqlAddFund)) {
                    psFund.setBigDecimal(1, amt);
                    psFund.setLong(2, loanId);
                    psFund.setBigDecimal(3, amt);
                    if (psFund.executeUpdate() == 0) {
                        conn.rollback(); // Gói vay đầy hoặc không ở trạng thái gọi vốn
                        return false;
                    }
                }

                // 4. Đánh dấu trạng thái đầu tư hoàn thành
                try (PreparedStatement psUpInv = conn.prepareStatement(sqlUpdateInvStatus)) {
                    psUpInv.setLong(1, investmentId);
                    psUpInv.executeUpdate();
                }

                conn.commit();

                // 5. Kiểm tra nếu đã gom đủ 100% thì tự động đóng gói chuyển sang 'process'
                Loan loan = getLoanById(loanId);
                if (loan != null && loan.isFullyFunded()) {
                    markFullyFunded(loanId);
                    try (PreparedStatement psN = conn.prepareStatement(sqlNotif)) {
                        psN.setLong(1, loan.getBorrowerId());
                        psN.setString(2, "Gọi vốn thành công");
                        psN.setString(3, "Gói vay #" + loanId + " đã gom đủ 100% vốn, chuyển sang gói đang xử lý.");
                        psN.executeUpdate();
                    }
                }
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
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