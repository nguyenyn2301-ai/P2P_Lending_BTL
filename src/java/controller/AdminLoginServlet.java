package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import dao.UserDAO;
import model.User;

@WebServlet(name = "AdminLoginServlet", urlPatterns = {"/AdminLoginServlet"})
public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Nếu người dùng cố tình truy cập link trực tiếp, đẩy về trang đăng nhập
        response.sendRedirect("admin_login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Nhận thông tin đăng nhập từ form gửi lên
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        UserDAO userDAO = new UserDAO();
        User admin = userDAO.adminLogin(email, password);

        if (admin != null) {
            HttpSession session = request.getSession();
            session.setAttribute("adminEmail", admin.getEmail());
            session.setAttribute("adminId", admin.getUser_id());
            session.setAttribute("role", "admin");
            response.sendRedirect(request.getContextPath() + "/AdminDashboardServlet");
        } else {
            // Đăng nhập thất bại -> Trả về thông báo lỗi và load lại trang đăng nhập admin
            request.setAttribute("errorMessage", "Tài khoản hoặc Mật khẩu quản trị viên không đúng!");
            request.getRequestDispatcher("admin_login.jsp").forward(request, response);
        }
    }
}