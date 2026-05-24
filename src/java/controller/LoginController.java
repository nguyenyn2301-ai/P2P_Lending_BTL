package controller;

import dao.UserDAO;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/LoginController") 
public class LoginController extends HttpServlet {
    
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin!");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        String trimmedEmail = email.trim();

        boolean isEmailExist = userDAO.checkEmailExist(trimmedEmail); 
        
        if (!isEmailExist) {
            request.setAttribute("errorMessage", "Tài khoản chưa được đăng ký trong hệ thống!");
            request.setAttribute("oldEmail", email); 
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return; 
        }

        User result = userDAO.loginCheck(trimmedEmail, password);

        if (result != null) {
            HttpSession session = request.getSession();
            
            long userId = result.getUser_id();  
            String userEmail = result.getEmail();
            String role = result.getRole();

            session.setAttribute("userId", userId);
            session.setAttribute("email", userEmail);
            session.setAttribute("role", role);

            if ("admin".equals(role)) {
                session.setAttribute("adminEmail", userEmail);
                session.setAttribute("adminId", userId);
                response.sendRedirect(request.getContextPath() + "/AdminDashboardServlet");
                return;
            }

            String accountStatus = userDAO.getUserAccountStatus(userId);
            if (accountStatus == null || !"active".equalsIgnoreCase(accountStatus)) {
                session.invalidate();
                request.setAttribute("errorMessage", "Tài khoản chưa được kích hoạt hoặc đã bị tạm khóa. Vui lòng liên hệ quản trị viên.");
                request.setAttribute("oldEmail", email);
                request.getRequestDispatcher("login.jsp").forward(request, response);
                return;
            }

            String ekycStatus = userDAO.getEkycStatus(userId);
            session.setAttribute("verification_status", ekycStatus != null ? ekycStatus : "none");

            // Kiểm tra tài khoản đã từng nộp hồ sơ eKYC (bất kể trạng thái) chưa
            boolean hasCompletedEkyc = userDAO.checkUserEKYC(userId);

            if ("borrower".equals(role)) {
                if (hasCompletedEkyc) {
                    response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=dashboard");
                } else {
                    response.sendRedirect("ekyc.jsp");
                }
            } 
            else if ("investor".equals(role)) {
                if (hasCompletedEkyc) {
                    response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?action=dashboard");
                } else {
                    response.sendRedirect("ekyc.jsp");
                }
            } 
            else {
                request.setAttribute("errorMessage", "Vai trò hệ thống của tài khoản không hợp lệ!");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
            
        } else {
            request.setAttribute("errorMessage", "Mật khẩu không chính xác! Vui lòng thử lại.");
            request.setAttribute("oldEmail", email); 
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendRedirect("login.jsp");
    }
}
