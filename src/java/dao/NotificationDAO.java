package dao;

import model.Notification;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean addNotification(long userId, String title, String message) {
        String sql = "INSERT INTO notifications (user_id, title, message, is_read) VALUES (?, ?, ?, FALSE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(3, message);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Notification> getNotificationsByUserId(long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, title, message, is_read, created_at "
                + "FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 30";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(rs.getLong("notification_id"));
                    n.setUserId(rs.getLong("user_id"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void notifyLoanParties(long loanId, String title, String message, LoanDAO loanDAO) {
        try {
            model.Loan loan = loanDAO.getLoanById(loanId);
            if (loan == null) return;
            addNotification(loan.getBorrowerId(), title, message);
            InvestmentDAO invDao = new InvestmentDAO();
            for (model.Investment inv : invDao.getByLoanId(loanId)) {
                addNotification(inv.getInvestorId(), title, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
