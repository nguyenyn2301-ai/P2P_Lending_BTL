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
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/admin_login.jsp");
            return;
        }

        String action = request.getParameter("action");
        LoanDAO loanDAO = new LoanDAO();
        long applicationId = Long.parseLong(request.getParameter("applicationId"));
        double rate = 12.0;
        try {
            rate = Double.parseDouble(request.getParameter("interestRate"));
        } catch (Exception ignored) {}

        if ("approve".equals(action)) {
            loanDAO.updateLoanStatus(applicationId, "approved", rate);
        } else if ("reject".equals(action)) {
            loanDAO.updateLoanStatus(applicationId, "rejected", 0);
        }

        response.sendRedirect(request.getContextPath() + "/AdminDashboardServlet?section=loan_new");
    }
}
