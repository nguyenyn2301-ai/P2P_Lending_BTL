package controller;

import dao.InvestorDAO;
import dao.LoanDAO;
import dao.NotificationDAO;
import dao.UserDAO;
import model.Investor;
import model.LoanApplication;
import model.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/InvestorLoanDetailServlet")
public class InvestorLoanDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Long userId = session != null ? (Long) session.getAttribute("userId") : null;
        String role = session != null ? (String) session.getAttribute("role") : null;

        if (userId == null || !"investor".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String loanIdParam = request.getParameter("loanId");
        if (loanIdParam == null || loanIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?action=market");
            return;
        }

        long loanId;
        try {
            loanId = Long.parseLong(loanIdParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?action=market");
            return;
        }

        UserDAO userDAO = new UserDAO();
        String ekycStatus = userDAO.getEkycStatus(userId);
        if (!"verified".equalsIgnoreCase(ekycStatus)) {
            response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?action=market&msg=ekyc");
            return;
        }

        LoanDAO loanDAO = new LoanDAO();
        LoanApplication loanDetail = loanDAO.getFundingLoanDetailForInvestor(loanId);
        if (loanDetail == null) {
            loanDetail = loanDAO.getLoanDetailForInvestor(loanId);
        }
        if (loanDetail == null) {
            response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?action=market&msg=invalid");
            return;
        }

        InvestorDAO investorDAO = new InvestorDAO();
        Investor investor = investorDAO.getById(userId);
        List<Notification> notifications = new NotificationDAO().getNotificationsByUserId(userId);

        request.setAttribute("investor", investor);
        request.setAttribute("investorName", investor != null ? investor.getFullName() : "Nhà đầu tư");
        request.setAttribute("loanDetail", loanDetail);
        request.setAttribute("notifications", notifications);
        request.getRequestDispatcher("/investor_loan_detail.jsp").forward(request, response);
    }
}
