package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/p2p_lending_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            String username = "root"; 
            String password = "123456"; 

            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Khong tim thay Driver MySQL! Hãy kiem tra lai file .jar trong Libraries.");
        }
    }

    public static void main(String[] args) {
        String sql = "SELECT user_id, email, role, status, created_at FROM users";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            System.out.printf("%-5s | %-25s | %-10s | %-10s | %-20s\n", "ID", "Email", "Role", "Status", "Created At");
            System.out.println("--------------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-5d | %-25s | %-10s | %-10s | %-20s\n",
                    rs.getLong("user_id"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            System.out.println("LOI! Truy van that bai: " + e.getMessage());
        }
    }
}