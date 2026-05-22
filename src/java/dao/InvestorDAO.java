package dao;

import model.Investor;
import util.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InvestorDAO {

    public Investor getById(long investorId) {
        String sql = "SELECT i.investor_id, i.first_name, i.last_name, i.wallet_balance, i.frozen_balance, "
                + "i.risk_appetite, i.verification_status, u.email "
                + "FROM investors i INNER JOIN users u ON i.investor_id = u.user_id WHERE i.investor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, investorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateWalletBalance(long investorId, BigDecimal newBalance) {
        String sql = "UPDATE investors SET wallet_balance = ? WHERE investor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, investorId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateFrozenBalance(long investorId, BigDecimal newFrozen) {
        String sql = "UPDATE investors SET frozen_balance = ? WHERE investor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newFrozen);
            ps.setLong(2, investorId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deductWalletAndAddFrozen(long investorId, BigDecimal amount) {
        String sql = "UPDATE investors SET wallet_balance = wallet_balance - ?, "
                + "frozen_balance = frozen_balance + ? WHERE investor_id = ? AND wallet_balance >= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setBigDecimal(2, amount);
            ps.setLong(3, investorId);
            ps.setBigDecimal(4, amount);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean releaseFrozen(long investorId, BigDecimal amount) {
        String sql = "UPDATE investors SET frozen_balance = frozen_balance - ? WHERE investor_id = ? AND frozen_balance >= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, investorId);
            ps.setBigDecimal(3, amount);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addWallet(long investorId, BigDecimal amount) {
        String sql = "UPDATE investors SET wallet_balance = wallet_balance + ? WHERE investor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setLong(2, investorId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Investor mapRow(ResultSet rs) throws Exception {
        Investor inv = new Investor();
        inv.setInvestorId(rs.getLong("investor_id"));
        inv.setFirstName(rs.getString("first_name"));
        inv.setLastName(rs.getString("last_name"));
        inv.setWalletBalance(rs.getBigDecimal("wallet_balance"));
        inv.setFrozenBalance(rs.getBigDecimal("frozen_balance"));
        inv.setRiskAppetite(rs.getString("risk_appetite"));
        inv.setVerificationStatus(rs.getString("verification_status"));
        inv.setEmail(rs.getString("email"));
        return inv;
    }
}
