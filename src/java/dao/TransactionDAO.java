package dao;

import model.Transaction;
import util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public boolean insertTransaction(long userId, BigDecimal amount, String type, String status) {
        String sql = "INSERT INTO transactions (user_id, amount, transaction_type, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, type);
            ps.setString(4, status);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Transaction> getByUserId(long userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transaction_id, user_id, amount, transaction_type, status, created_at "
                + "FROM transactions WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTransactionId(rs.getLong("transaction_id"));
                    t.setUserId(rs.getLong("user_id"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setTransactionType(rs.getString("transaction_type"));
                    t.setStatus(rs.getString("status"));
                    t.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
