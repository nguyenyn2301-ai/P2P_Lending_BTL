package dao;

import model.WalletDeposit;
import util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletDepositDAO {

    public long insertDeposit(long investorId, BigDecimal amount, String transferContent, String cicPdfUrl) {
        String sql = "INSERT INTO wallet_deposits (investor_id, amount, transfer_content, cic_pdf_url, status) "
                + "VALUES (?, ?, ?, ?, 'pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, investorId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, transferContent);
            ps.setString(4, cicPdfUrl);
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getLong(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public WalletDeposit getById(long depositId) {
        String sql = "SELECT d.deposit_id, d.investor_id, d.amount, d.transfer_content, d.cic_pdf_url, "
                + "d.status, d.created_at, CONCAT(i.first_name, ' ', i.last_name) AS investor_name "
                + "FROM wallet_deposits d "
                + "INNER JOIN investors i ON d.investor_id = i.investor_id WHERE d.deposit_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, depositId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<WalletDeposit> getPendingDeposits() {
        List<WalletDeposit> list = new ArrayList<>();
        String sql = "SELECT d.deposit_id, d.investor_id, d.amount, d.transfer_content, d.cic_pdf_url, "
                + "d.status, d.created_at, CONCAT(i.first_name, ' ', i.last_name) AS investor_name "
                + "FROM wallet_deposits d "
                + "INNER JOIN investors i ON d.investor_id = i.investor_id "
                + "WHERE d.status = 'pending' ORDER BY d.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean approveDeposit(long depositId) {
        WalletDeposit deposit = getById(depositId);
        if (deposit == null || !"pending".equals(deposit.getStatus())) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String updateSql = "UPDATE wallet_deposits SET status = 'approved' WHERE deposit_id = ? AND status = 'pending'";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setLong(1, depositId);
                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }

            String walletSql = "UPDATE investors SET wallet_balance = wallet_balance + ? WHERE investor_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(walletSql)) {
                ps.setBigDecimal(1, deposit.getAmount());
                ps.setLong(2, deposit.getInvestorId());
                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();

            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            String amountStr = nf.format(deposit.getAmount());
            NotificationDAO notifDAO = new NotificationDAO();
            notifDAO.addNotification(deposit.getInvestorId(), "Nạp tiền thành công",
                    "Đã nạp + " + amountStr + " thành công");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
        return false;
    }

    private WalletDeposit mapRow(ResultSet rs) throws SQLException {
        WalletDeposit d = new WalletDeposit();
        d.setDepositId(rs.getLong("deposit_id"));
        d.setInvestorId(rs.getLong("investor_id"));
        d.setAmount(rs.getBigDecimal("amount"));
        d.setTransferContent(rs.getString("transfer_content"));
        d.setCicPdfUrl(rs.getString("cic_pdf_url"));
        d.setStatus(rs.getString("status"));
        d.setCreatedAt(rs.getTimestamp("created_at"));
        d.setInvestorName(rs.getString("investor_name"));
        return d;
    }
}
