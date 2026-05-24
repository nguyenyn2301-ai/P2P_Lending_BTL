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

@WebServlet("/RegisterController")
public class RegisterController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = request.getParameter("role"); // 'borrower' hoặc 'investor'
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String agreeTerms = request.getParameter("agreeTerms");

        if (!"on".equals(agreeTerms)) {
            response.sendRedirect("register.jsp?error=terms");
            return;
        }

        // 1. Kiểm tra các trường thông tin chung bắt buộc
        if (email == null || password == null || confirmPassword == null || role == null || firstName == null || lastName == null
                || email.trim().isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty() 
                || role.trim().isEmpty() || firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            response.sendRedirect("register.jsp?error=missingFields");
            return;
        }

        // 2. Kiểm tra mật khẩu khớp nhau (Backend Validation đề phòng bypass Client)
        if (!password.equals(confirmPassword)) {
            response.sendRedirect("register.jsp?error=passwordMismatch");
            return;
        }

        // 3. Kiểm tra trùng lặp email trước khi chèn vào DB
        if (userDAO.checkEmailExist(email.trim())) {
            response.sendRedirect("register.jsp?error=emailExist");
            return;
        }

        boolean registerStatus = false;

        // 4. Xử lý dữ liệu đặc thù theo từng Vai trò
        try {
            if ("borrower".equals(role)) {
                String idCardNumber = request.getParameter("idCardNumber");
                String monthlyIncomeStr = request.getParameter("monthlyIncome");

                if (idCardNumber == null || idCardNumber.trim().isEmpty() || monthlyIncomeStr == null || monthlyIncomeStr.trim().isEmpty()) {
                    response.sendRedirect("register.jsp?error=missingFields");
                    return;
                }

                double monthlyIncome;
                try {
                    monthlyIncome = Double.parseDouble(monthlyIncomeStr.trim());
                } catch (NumberFormatException e) {
                    response.sendRedirect("register.jsp?error=invalidIncome");
                    return;
                }

                registerStatus = userDAO.registerUser(email.trim(), password, role, firstName.trim(), lastName.trim(), idCardNumber.trim(), monthlyIncome, null);

            } else if ("investor".equals(role)) {
                String riskAppetite = request.getParameter("riskAppetite");

                if (riskAppetite == null || riskAppetite.trim().isEmpty()) {
                    response.sendRedirect("register.jsp?error=missingFields");
                    return;
                }

                registerStatus = userDAO.registerUser(email.trim(), password, role, firstName.trim(), lastName.trim(), null, 0.0, riskAppetite.trim());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("register.jsp?error=failed");
            return;
        }

        if (registerStatus) {
            User newUser = userDAO.loginCheck(email.trim(), password);
            if (newUser != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", newUser.getUser_id());
                session.setAttribute("email", newUser.getEmail());
                session.setAttribute("role", newUser.getRole());
                session.setAttribute("verification_status", "pending");
                response.sendRedirect(request.getContextPath() + "/ekyc.jsp?step=profile");
                return;
            }
            response.sendRedirect("login.jsp?success=registered");
        } else {
            response.sendRedirect("register.jsp?error=failed");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("register.jsp");
    }
}
