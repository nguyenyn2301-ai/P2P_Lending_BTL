package dao; 

import util.DBConnection; 
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
        String sql = "SELECT app.application_id, app.amount_requested, app.term_months, app.created_at, " +
                     "       u.full_name, l.interest_rate, l.current_funded, l.status AS loan_status " +
                     "FROM loans l " +
                     "INNER JOIN loan_applications app ON l.application_id = app.application_id " +
                     "INNER JOIN borrowers b ON app.borrower_id = b.borrower_id " +
                     "INNER JOIN users u ON b.user_id = u.user_id " +
                     "WHERE l.status = 'funding' ORDER BY l.loan_id DESC";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                LoanApplication loan = new LoanApplication();
                loan.setApplicationId(rs.getLong("application_id"));
                loan.setAmountRequested(rs.getBigDecimal("amount_requested"));
                loan.setTermMonths(rs.getInt("term_months"));
                loan.setCreatedAt(rs.getTimestamp("created_at"));
                
                // Xử lý ẩn danh tên (Ví dụ: "Hà Phương Linh" -> H****** Linh) để bảo mật trên sàn công khai
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
        String sqlInsertLoan = "INSERT INTO loans (application_id, total_amount, current_funded, interest_rate, status, updated_at) " +
                              "SELECT application_id, amount_requested, ?, ?, 'funding', NOW() " +
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
}