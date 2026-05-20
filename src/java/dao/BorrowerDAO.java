package dao;

import util.DBConnection;
import model.Borrower;
import model.LoanApplication;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BorrowerDAO {

    // =========================================================================
    // 1. LẤY THÔNG TIN CƠ BẢN CỦA BORROWER (GIAO DIỆN DASHBOARD)
    // =========================================================================
    public Borrower getBorrowerById(long borrowerId) {
        String sql = "SELECT borrower_id, first_name, last_name, verification_status, monthly_income, " +
                     "id_card_number FROM borrowers WHERE borrower_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, borrowerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Borrower b = new Borrower();
                    b.setBorrowerId(rs.getLong("borrower_id"));
                    b.setFirstName(rs.getString("first_name"));
                    b.setLastName(rs.getString("last_name"));
                    
                    // Chuẩn hóa hiển thị trạng thái eKYC ra giao diện Tiếng Việt
                    String vStatus = rs.getString("verification_status");
                    if ("pending".equalsIgnoreCase(vStatus)) b.setVerificationStatus("Chờ duyệt");
                    else if ("verified".equalsIgnoreCase(vStatus)) b.setVerificationStatus("Đã xác thực");
                    else if ("rejected".equalsIgnoreCase(vStatus)) b.setVerificationStatus("Bị từ chối");
                    else b.setVerificationStatus(vStatus);
                    
                    // Đồng bộ kiểu dữ liệu BigDecimal sang double (hoặc giữ nguyên nếu Model là BigDecimal)
                    BigDecimal incomeBg = rs.getBigDecimal("monthly_income");
                    b.setMonthlyIncome(incomeBg != null ? incomeBg.doubleValue() : 0.0);
                    
                    b.setIdCardNumber(rs.getString("id_card_number"));
                    return b;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================================
    // 2. TÍNH TỔNG DƯ NỢ THỰC TẾ (LỌC THEO STATUS ACTIVE CỦA KHOẢN VAY)
    // =========================================================================
    public double getCurrentDebt(long borrowerId) {
        double totalDebt = 0.0;
        String sql = "SELECT SUM(l.total_amount) FROM loans l " +
                     "INNER JOIN loan_applications la ON l.application_id = la.application_id " +
                     "WHERE la.borrower_id = ? AND l.status = 'active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, borrowerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal debtBg = rs.getBigDecimal(1);
                    if (debtBg != null) {
                        totalDebt = debtBg.doubleValue();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalDebt;
    }

    // =========================================================================
    // 3. LẤY DANH SÁCH ĐƠN VAY CÁ NHÂN (ĐỒNG BỘ ĐỊNH DẠNG MỚI)
    // =========================================================================
    public List<LoanApplication> getLoansByBorrower(long borrowerId) {
        List<LoanApplication> list = new ArrayList<>();
        String sql = "SELECT application_id, amount_requested, term_months, created_at, cic_pdf_url, status " +
                     "FROM loan_applications WHERE borrower_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, borrowerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LoanApplication loan = new LoanApplication();
                    
                    loan.setApplicationId(rs.getLong("application_id")); 
                    loan.setAmountRequested(rs.getBigDecimal("amount_requested")); 
                    loan.setTermMonths(rs.getInt("term_months")); 
                    loan.setCreatedAt(rs.getTimestamp("created_at"));
                    loan.setCicPdfUrl(rs.getString("cic_pdf_url")); 
                    
                    // Ánh xạ trạng thái hiển thị tiếng Việt đồng bộ với hệ thống chính
                    String dbStatus = rs.getString("status");
                    if ("pending".equalsIgnoreCase(dbStatus)) loan.setStatus("Chờ duyệt");
                    else if ("approved".equalsIgnoreCase(dbStatus)) loan.setStatus("Đã duyệt");
                    else if ("rejected".equalsIgnoreCase(dbStatus)) loan.setStatus("Bị từ chối");
                    else if ("funded".equalsIgnoreCase(dbStatus)) loan.setStatus("Đã gọi vốn xong");
                    else loan.setStatus(dbStatus);
                    
                    list.add(loan);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // 4. LƯU HOẶC GHI ĐÈ FILE ĐỊNH DANH EKYC (TRÁNH RÁC DATABASE)
    // =========================================================================
    public boolean saveOrUpdateDocument(long userId, String type, String fileUrl) {
        String checkSql = "SELECT COUNT(*) FROM documents WHERE user_id = ? AND document_type = ?";
        String insertSql = "INSERT INTO documents (user_id, document_type, file_url, uploaded_at) VALUES (?, ?, ?, NOW())";
        String updateSql = "UPDATE documents SET file_url = ?, uploaded_at = NOW() WHERE user_id = ? AND document_type = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            boolean exists = false;
            
            // Bước 4.1: Kiểm tra sự tồn tại của cấu trúc chứng từ
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setLong(1, userId);
                psCheck.setString(2, type);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        exists = true;
                    }
                }
            }
            
            // Bước 4.2: Thực thi cập nhật hoặc chèn mới linh hoạt
            if (exists) {
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setString(1, fileUrl);
                    psUpdate.setLong(2, userId);
                    psUpdate.setString(3, type);
                    return psUpdate.executeUpdate() > 0;
                }
            } else {
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setLong(1, userId);
                    psInsert.setString(2, type);
                    psInsert.setString(3, fileUrl);
                    return psInsert.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // 5. CẬP NHẬT TRẠNG THÁI XÁC THỰC EKYC (ĐỒNG BỘ THEO CHUẨN ENUM)
    // =========================================================================
    public boolean updateEkycStatus(long borrowerId, String status) {
        String sql = "UPDATE borrowers SET verification_status = ? WHERE borrower_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Luôn lưu trạng thái tiếng Anh viết thường vào DB ('pending', 'verified', 'rejected')
            ps.setString(1, status.toLowerCase().trim());
            ps.setLong(2, borrowerId);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}