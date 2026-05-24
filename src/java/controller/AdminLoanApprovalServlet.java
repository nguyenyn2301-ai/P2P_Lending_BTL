package controller;

import dao.LoanDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/AdminLoanApprovalServlet")
public class AdminLoanApprovalServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Kiểm tra quyền Admin trong Session
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 2. Đọc các tham số gửi lên từ Form phê duyệt đơn vay
        String action = request.getParameter("action");
        String appIdParam = request.getParameter("applicationId");
        String rateParam = request.getParameter("interestRate");

        // 3. Kiểm tra dữ liệu đầu vào tránh lỗi null hoặc chuỗi trống gây crash
        if (appIdParam == null || appIdParam.trim().isEmpty()) {
            session.setAttribute("message", "Thiếu ID đơn vay cần xử lý!");
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/AdminDashboardServlet?section=loan_new");
            return;
        }

        long applicationId = 0;
        double rate = 12.0; // Lãi suất sàn mặc định nếu admin không nhập hoặc nhập lỗi

        // 4. Khắc phục lỗi 500: Bọc bẫy lỗi NumberFormatException khi ép kiểu dữ liệu số
        try {
            applicationId = Long.parseLong(appIdParam);
            if (rateParam != null && !rateParam.trim().isEmpty()) {
                rate = Double.parseDouble(rateParam);
            }
        } catch (NumberFormatException e) {
            // Flash Message báo lỗi định dạng dữ liệu
            session.setAttribute("message", "Định dạng ID hoặc lãi suất không hợp lệ!");
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/AdminDashboardServlet?section=loan_new");
            return;
        }

        // 5. Khởi tạo DAO và thực thi xử lý nghiệp vụ thay đổi trạng thái đơn vay
        LoanDAO loanDAO = new LoanDAO();
        boolean success = false;

        if ("approve".equals(action)) {
            // Thực thi phê duyệt hồ sơ
            success = loanDAO.updateLoanStatus(applicationId, "approved", rate);
            if (success) {
                session.setAttribute("message", "Đã phê duyệt hồ sơ và đẩy khoản vay lên sàn thành công!");
                session.setAttribute("messageType", "success");
            } else {
                session.setAttribute("message", "Duyệt hồ sơ thất bại! Vui lòng kiểm tra lại hệ thống.");
                session.setAttribute("messageType", "danger");
            }
        } else if ("reject".equals(action)) {
            // Thực thi từ chối hồ sơ (Lãi suất truyền vào = 0)
            success = loanDAO.updateLoanStatus(applicationId, "rejected", 0);
            if (success) {
                session.setAttribute("message", "Đã từ chối đơn đăng ký vay thành công.");
                session.setAttribute("messageType", "warning"); // Màu vàng cảnh báo
            } else {
                session.setAttribute("message", "Thao tác từ chối đơn vay thất bại!");
                session.setAttribute("messageType", "danger");
            }
        }

        // 6. Redirect an toàn về màn hình danh sách đơn vay mới của Admin
        response.sendRedirect(request.getContextPath() + "/AdminDashboardServlet?section=loan_new");
    }
}