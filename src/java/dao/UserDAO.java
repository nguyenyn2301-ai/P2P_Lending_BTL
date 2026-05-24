package dao;

import model.User;
import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ==========================================
    // PHẦN 1: PHỤC VỤ CHO REGISTERCONTROLLER
    // ==========================================

    public boolean checkEmailExist(String email) {
        String sql = "SELECT user_id FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerUser(String email, String password, String role, 
                                String firstName, String lastName, 
                                String idCardNumber, double monthlyIncome, 
                                String riskAppetite) {
        Connection conn = null;
        PreparedStatement psUser = null;
        PreparedStatement psProfile = null;
        ResultSet rsKeys = null;

        String sqlUser = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
        String sqlBorrower = "INSERT INTO borrowers (borrower_id, first_name, last_name, id_card_number, monthly_income, wallet_balance, verification_status, risk_level) "
                           + "VALUES (?, ?, ?, ?, ?, 0.00, 'pending', 'Medium')";
        
        String sqlInvestor = "INSERT INTO investors (investor_id, first_name, last_name, wallet_balance, frozen_balance, risk_appetite, verification_status) "
                           + "VALUES (?, ?, ?, 0.00, 0.00, ?, 'pending')";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            psUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            psUser.setString(1, email);
            psUser.setString(2, password);
            psUser.setString(3, role);
            psUser.executeUpdate();

            rsKeys = psUser.getGeneratedKeys();
            long generatedUserId = 0;
            if (rsKeys.next()) {
                generatedUserId = rsKeys.getLong(1);
            } else {
                throw new Exception("Không lấy được ID từ bảng users.");
            }

            if ("borrower".equals(role)) {
                psProfile = conn.prepareStatement(sqlBorrower);
                psProfile.setLong(1, generatedUserId);
                psProfile.setString(2, firstName);
                psProfile.setString(3, lastName);
                psProfile.setString(4, idCardNumber);
                psProfile.setDouble(5, monthlyIncome);
                psProfile.executeUpdate();
            } 
            else if ("investor".equals(role)) {
                psProfile = conn.prepareStatement(sqlInvestor);
                psProfile.setLong(1, generatedUserId);
                psProfile.setString(2, firstName);
                psProfile.setString(3, lastName);
                psProfile.setString(4, riskAppetite); 
                psProfile.executeUpdate();
            }

            conn.commit(); 
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            try { if (rsKeys != null) rsKeys.close(); } catch (Exception e) {}
            try { if (psUser != null) psUser.close(); } catch (Exception e) {}
            try { if (psProfile != null) psProfile.close(); } catch (Exception e) {}
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception e) {}
        }
    }

    // ==========================================
    // PHẦN 2: PHỤC VỤ CHO LOGINCONTROLLER
    // ==========================================

    public User loginCheck(String email, String password) {
        String sql = "SELECT user_id, email, role FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUser_id(rs.getLong("user_id")); 
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * TỐI ƯU LOGIC ĐĂNG NHẬP LẦN 2 (ĐỒNG BỘ THEO ENUM DATABASE):
     * Kiểm tra tài khoản đã gửi đủ 3 ảnh bắt buộc của eKYC chưa.
     */
    public boolean checkUserEKYC(long userId) {
        String sqlCountDocs = "SELECT COUNT(DISTINCT document_type) FROM documents WHERE user_id = ? "
                            + "AND document_type IN ('id_card_front', 'id_card_back', 'other')";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psDocs = conn.prepareStatement(sqlCountDocs)) {
            
            psDocs.setLong(1, userId);
            try (ResultSet rs = psDocs.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) >= 3; 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUserAccountStatus(long userId) {
        String sql = "SELECT status FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEkycStatus(long userId) {
        String sqlRole = "SELECT role FROM users WHERE user_id = ?";
        String sqlBorrower = "SELECT verification_status FROM borrowers WHERE borrower_id = ?";
        String sqlInvestor = "SELECT verification_status FROM investors WHERE investor_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            String role = "";
            try (PreparedStatement psRole = conn.prepareStatement(sqlRole)) {
                psRole.setLong(1, userId);
                try (ResultSet rsRole = psRole.executeQuery()) {
                    if (rsRole.next()) role = rsRole.getString("role");
                }
            }

            if ("borrower".equals(role)) {
                try (PreparedStatement psB = conn.prepareStatement(sqlBorrower)) {
                    psB.setLong(1, userId);
                    try (ResultSet rsB = psB.executeQuery()) {
                        if (rsB.next()) return rsB.getString("verification_status");
                    }
                }
            } else if ("investor".equals(role)) {
                try (PreparedStatement psI = conn.prepareStatement(sqlInvestor)) {
                    psI.setLong(1, userId);
                    try (ResultSet rsI = psI.executeQuery()) {
                        if (rsI.next()) return rsI.getString("verification_status");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "none";
    }

    // ==========================================
    // PHẦN 3: PHỤC VỤ CHO EKYCCONTROLLER / UPLOAD
    // ==========================================

    /**
     * GIẢI QUYẾT LỖI BIÊN DỊCH (GHI ĐÈ):
     * Hàm tự động kiểm tra xem loại tài liệu (front, back, other) đã tồn tại của user chưa.
     * - Nếu ĐÃ CÓ: Tiến hành UPDATE đường dẫn ảnh mới (Ghi đè để tránh rác DB khi re-eKYC).
     * - Nếu CHƯA CÓ: Tiến hành INSERT mới hoàn toàn.
     */
    public boolean saveOrUpdateDocument(Long userId, String type, String fileUrl) {
        String checkSql = "SELECT COUNT(*) FROM documents WHERE user_id = ? AND document_type = ?";
        String insertSql = "INSERT INTO documents (user_id, document_type, file_url) VALUES (?, ?, ?)";
        String updateSql = "UPDATE documents SET file_url = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND document_type = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            // 1. Kiểm tra sự tồn tại của cấu hình loại ảnh thuộc User
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setLong(1, userId);
                psCheck.setString(2, type);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // 2. Thực hiện UPDATE nếu bản ghi đã tồn tại trước đó
                        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                            psUpdate.setString(1, fileUrl);
                            psUpdate.setLong(2, userId);
                            psUpdate.setString(3, type);
                            return psUpdate.executeUpdate() > 0;
                        }
                    } else {
                        // 3. Thực hiện INSERT nếu người dùng làm eKYC lần đầu tiên
                        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                            psInsert.setLong(1, userId);
                            psInsert.setString(2, type);
                            psInsert.setString(3, fileUrl);
                            return psInsert.executeUpdate() > 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean insertDocument(Long userId, String type, String url) {
        String sql = "INSERT INTO documents (user_id, document_type, file_url) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, type);
            ps.setString(3, url);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateOrInsertEkycStatus(long userId, String status) {
        String sqlRole = "SELECT role FROM users WHERE user_id = ?";
        String sqlBorrower = "UPDATE borrowers SET verification_status = ? WHERE borrower_id = ?";
        String sqlInvestor = "UPDATE investors SET verification_status = ? WHERE investor_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            String role = "";
            try (PreparedStatement psRole = conn.prepareStatement(sqlRole)) {
                psRole.setLong(1, userId);
                try (ResultSet rsRole = psRole.executeQuery()) {
                    if (rsRole.next()) role = rsRole.getString("role");
                }
            }

            if ("borrower".equals(role)) {
                try (PreparedStatement ps = conn.prepareStatement(sqlBorrower)) {
                    ps.setString(1, status);
                    ps.setLong(2, userId);
                    return ps.executeUpdate() > 0;
                }
            } else if ("investor".equals(role)) {
                try (PreparedStatement ps = conn.prepareStatement(sqlInvestor)) {
                    ps.setString(1, status);
                    ps.setLong(2, userId);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // PHẦN 4: ADMIN & QUẢN LÝ NGƯỜI DÙNG 
    // =========================================================================

    public User adminLogin(String email, String password) {
        String sql = "SELECT user_id, email, role FROM users WHERE email = ? AND password = ? AND role = 'admin'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUser_id(rs.getLong("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public long getAdminUserId() {
        String sql = "SELECT user_id FROM users WHERE role = 'admin' ORDER BY user_id ASC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("user_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<java.util.Map<String, Object>> getPendingEkycUsers() {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        
        String sqlBorrower = "SELECT u.user_id, u.email, u.role, u.created_at, b.first_name, b.last_name, b.verification_status, "
                + "MAX(CASE WHEN d.document_type = 'id_card_front' THEN d.file_url END) as frontImg, "
                + "MAX(CASE WHEN d.document_type = 'id_card_back' THEN d.file_url END) as backImg, "
                + "MAX(CASE WHEN d.document_type = 'other' THEN d.file_url END) as selfieImg "
                + "FROM users u "
                + "INNER JOIN borrowers b ON u.user_id = b.borrower_id "
                + "LEFT JOIN documents d ON u.user_id = d.user_id "
                + "WHERE b.verification_status = 'pending' AND u.role = 'borrower' "
                + "GROUP BY u.user_id, u.email, u.role, u.created_at, b.first_name, b.last_name, b.verification_status";
                
        String sqlInvestor = "SELECT u.user_id, u.email, u.role, u.created_at, i.first_name, i.last_name, i.verification_status, "
                + "MAX(CASE WHEN d.document_type = 'id_card_front' THEN d.file_url END) as frontImg, "
                + "MAX(CASE WHEN d.document_type = 'id_card_back' THEN d.file_url END) as backImg, "
                + "MAX(CASE WHEN d.document_type = 'other' THEN d.file_url END) as selfieImg "
                + "FROM users u "
                + "INNER JOIN investors i ON u.user_id = i.investor_id "
                + "LEFT JOIN documents d ON u.user_id = d.user_id "
                + "WHERE i.verification_status = 'pending' AND u.role = 'investor' "
                + "GROUP BY u.user_id, u.email, u.role, u.created_at, i.first_name, i.last_name, i.verification_status";
                
        try (Connection conn = DBConnection.getConnection()) {
            appendEkycRows(conn, sqlBorrower, list);
            appendEkycRows(conn, sqlInvestor, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getEkycFilePathsByUserId(long userId) {
        List<String> paths = new ArrayList<>();
        String sql = "SELECT file_url FROM documents WHERE user_id = ? "
                + "AND document_type IN ('id_card_front', 'id_card_back', 'other')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String url = rs.getString("file_url");
                    if (url != null && !url.trim().isEmpty()) {
                        paths.add(url);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }

    public List<java.util.Map<String, Object>> getEkycDocumentsByUserId(long userId) {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT document_type, file_url, uploaded_at FROM documents WHERE user_id = ? "
                + "AND document_type IN ('id_card_front', 'id_card_back', 'other') ORDER BY document_type";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("documentType", rs.getString("document_type"));
                    m.put("fileUrl", rs.getString("file_url"));
                    m.put("uploadedAt", rs.getTimestamp("uploaded_at"));
                    list.add(m);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteEkycDocumentsForUser(long userId) {
        String sql = "DELETE FROM documents WHERE user_id = ? "
                + "AND document_type IN ('id_card_front', 'id_card_back', 'other')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void appendEkycRows(Connection conn, String sql, List<java.util.Map<String, Object>> list) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("userId", rs.getLong("user_id"));
                m.put("email", rs.getString("email"));
                m.put("role", rs.getString("role"));
                m.put("fullName", rs.getString("first_name") + " " + rs.getString("last_name"));
                m.put("verificationStatus", rs.getString("verification_status"));
                m.put("createdAt", rs.getTimestamp("created_at"));
                
                m.put("frontImg", rs.getString("frontImg"));
                m.put("backImg", rs.getString("backImg"));
                m.put("selfieImg", rs.getString("selfieImg"));
                
                list.add(m);
            }
        }
    }

    public List<java.util.Map<String, Object>> getAllUsersByRole(String roleFilter) {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        String base = "SELECT u.user_id, u.email, u.role, u.status, u.created_at FROM users u WHERE u.role != 'admin'";
        if (roleFilter != null && !roleFilter.isEmpty() && !"all".equalsIgnoreCase(roleFilter)) {
            base += " AND u.role = ?";
        }
        base += " ORDER BY u.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(base)) {
            if (roleFilter != null && !roleFilter.isEmpty() && !"all".equalsIgnoreCase(roleFilter)) {
                ps.setString(1, roleFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    long uid = rs.getLong("user_id");
                    m.put("userId", uid);
                    m.put("email", rs.getString("email"));
                    m.put("role", rs.getString("role"));
                    m.put("status", rs.getString("status"));
                    m.put("createdAt", rs.getTimestamp("created_at"));
                    m.put("profileDetail", getProfileDetail(conn, uid, rs.getString("role")));
                    list.add(m);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private String getProfileDetail(Connection conn, long userId, String role) throws Exception {
        if ("borrower".equals(role)) {
            String sql = "SELECT first_name, last_name, verification_status, monthly_income, wallet_balance FROM borrowers WHERE borrower_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("first_name") + " " + rs.getString("last_name")
                                + " | eKYC: " + rs.getString("verification_status")
                                + " | Thu nhập: " + rs.getBigDecimal("monthly_income")
                                + " | Ví: " + rs.getBigDecimal("wallet_balance");
                    }
                }
            }
        } else if ("investor".equals(role)) {
            String sql = "SELECT first_name, last_name, verification_status, wallet_balance, frozen_balance FROM investors WHERE investor_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("first_name") + " " + rs.getString("last_name")
                                + " | eKYC: " + rs.getString("verification_status")
                                + " | Ví: " + rs.getBigDecimal("wallet_balance")
                                + " | Đóng băng: " + rs.getBigDecimal("frozen_balance");
                    }
                }
            }
        }
        return "";
    }

    public boolean updateUserVerification(long userId, String status) {
        return updateOrInsertEkycStatus(userId, status);
    }

    /**
     * Xóa tài khoản người dùng (borrower/investor). Không xóa admin.
     */
    public boolean deleteUser(long userId) {
        String roleSql = "SELECT role FROM users WHERE user_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String role = null;
            try (PreparedStatement ps = conn.prepareStatement(roleSql)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                    role = rs.getString("role");
                }
            }
            if (role == null || "admin".equals(role)) {
                return false;
            }

            if ("borrower".equals(role)) {
                deleteBorrowerData(conn, userId);
            } else if ("investor".equals(role)) {
                deleteInvestorData(conn, userId);
            }

            execUpdate(conn, "DELETE FROM transactions WHERE user_id = ?", userId);
            execUpdate(conn, "DELETE FROM notifications WHERE user_id = ?", userId);
            execUpdate(conn, "DELETE FROM documents WHERE user_id = ?", userId);
            execUpdate(conn, "DELETE FROM bank_accounts WHERE user_id = ?", userId);

            if ("borrower".equals(role)) {
                execUpdate(conn, "DELETE FROM borrowers WHERE borrower_id = ?", userId);
            } else {
                execUpdate(conn, "DELETE FROM investors WHERE investor_id = ?", userId);
            }
            execUpdate(conn, "DELETE FROM users WHERE user_id = ? AND role != 'admin'", userId);

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    private void deleteBorrowerData(Connection conn, long borrowerId) throws Exception {
        String appsSql = "SELECT application_id FROM loan_applications WHERE borrower_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(appsSql)) {
            ps.setLong(1, borrowerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long appId = rs.getLong("application_id");
                    deleteLoansByApplication(conn, appId);
                    execUpdate(conn, "DELETE FROM loan_applications WHERE application_id = ?", appId);
                }
            }
        }
    }

    private void deleteInvestorData(Connection conn, long investorId) throws Exception {
        execUpdate(conn, "DELETE FROM investments WHERE investor_id = ?", investorId);
    }

    private void deleteLoansByApplication(Connection conn, long applicationId) throws Exception {
        String loansSql = "SELECT loan_id FROM loans WHERE application_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(loansSql)) {
            ps.setLong(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long loanId = rs.getLong("loan_id");
                    execUpdate(conn, "DELETE FROM investments WHERE loan_id = ?", loanId);
                    execUpdate(conn, "DELETE FROM loans WHERE loan_id = ?", loanId);
                }
            }
        }
    }

    private void execUpdate(Connection conn, String sql, long id) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}