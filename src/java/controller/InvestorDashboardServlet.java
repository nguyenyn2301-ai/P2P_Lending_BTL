package controller;

import dao.*;
import model.Investor;
import model.LoanApplication;
import model.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/InvestorDashboardServlet")
public class InvestorDashboardServlet extends HttpServlet {

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

        InvestorDAO investorDAO = new InvestorDAO();
        LoanDAO loanDAO = new LoanDAO();
        NotificationDAO notificationDAO = new NotificationDAO();

        Investor investor = investorDAO.getById(userId);
        List<LoanApplication> fundingLoans = loanDAO.getAllMarketLoans();
        List<Notification> notifications = notificationDAO.getNotificationsByUserId(userId);

        request.setAttribute("investor", investor);
        request.setAttribute("investorName", investor != null ? investor.getFullName() : "Nhà đầu tư");
        request.setAttribute("fundingLoans", fundingLoans);
        request.setAttribute("notifications", notifications);
        request.getRequestDispatcher("/investor_dashboard.jsp").forward(request, response);
    }
}
