package dao;

import model.Investment;
import util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestmentDAO {

    public boolean insertPendingInvestment(long loanId, long investorId, BigDecimal amount,
            String bankName, String accountNumber) {
        String sql = "INSERT INTO investments (loan_id, investor_id, amount_invested, status, source_bank_name, source_account_number) "
                + "VALUES (?, ?, ?, 'pending', ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            ps.setLong(2, investorId);
            ps.setBigDecimal(3, amount);
            ps.setString(4, bankName);
            ps.setString(5, accountNumber);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Investment> getPendingInvestments() {
        List<Investment> list = new ArrayList<>();
        String sql = "SELECT i.investment_id, i.loan_id, i.investor_id, i.amount_invested, i.status, i.invested_at, "
                + "CONCAT(inv.first_name, ' ', inv.last_name) AS investor_name, l.loan_code, l.total_amount "
                + "FROM investments i "
                + "INNER JOIN investors inv ON i.investor_id = inv.investor_id "
                + "INNER JOIN loans l ON i.loan_id = l.loan_id "
                + "WHERE i.status = 'pending' ORDER BY i.invested_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Investment getById(long investmentId) {
        String sql = "SELECT i.investment_id, i.loan_id, i.investor_id, i.amount_invested, i.status, i.invested_at, "
                + "CONCAT(inv.first_name, ' ', inv.last_name) AS investor_name, l.loan_code, l.total_amount "
                + "FROM investments i "
                + "INNER JOIN investors inv ON i.investor_id = inv.investor_id "
                + "INNER JOIN loans l ON i.loan_id = l.loan_id WHERE i.investment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, investmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatus(long investmentId, String status) {
        String sql = "UPDATE investments SET status = ? WHERE investment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, investmentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Investment> getByLoanId(long loanId) {
        List<Investment> list = new ArrayList<>();
        String sql = "SELECT i.investment_id, i.loan_id, i.investor_id, i.amount_invested, i.status, i.invested_at, "
                + "CONCAT(inv.first_name, ' ', inv.last_name) AS investor_name, l.loan_code, l.total_amount "
                + "FROM investments i "
                + "INNER JOIN investors inv ON i.investor_id = inv.investor_id "
                + "INNER JOIN loans l ON i.loan_id = l.loan_id WHERE i.loan_id = ? AND i.status = 'completed'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Investment mapRow(ResultSet rs) throws Exception {
        Investment inv = new Investment();
        inv.setInvestmentId(rs.getLong("investment_id"));
        inv.setLoanId(rs.getLong("loan_id"));
        inv.setInvestorId(rs.getLong("investor_id"));
        inv.setAmountInvested(rs.getBigDecimal("amount_invested"));
        inv.setStatus(rs.getString("status"));
        inv.setInvestedAt(rs.getTimestamp("invested_at"));
        inv.setInvestorName(rs.getString("investor_name"));
        inv.setLoanCode(rs.getString("loan_code"));
        inv.setLoanTotalAmount(rs.getBigDecimal("total_amount"));
        return inv;
    }
}
