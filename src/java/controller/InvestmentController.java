package controller;

import dao.*;
import model.Investor;
import model.Loan;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/InvestmentController")
public class InvestmentController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Long userId = session != null ? (Long) session.getAttribute("userId") : null;
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        long loanId = Long.parseLong(request.getParameter("loanId"));
        BigDecimal amount = new BigDecimal(request.getParameter("amount"));
        String bankName = request.getParameter("bankName");
        String accountNumber = request.getParameter("accountNumber");

        InvestorDAO investorDAO = new InvestorDAO();
        LoanDAO loanDAO = new LoanDAO();
        InvestmentDAO investmentDAO = new InvestmentDAO();
        NotificationDAO notifDAO = new NotificationDAO();

        Investor inv = investorDAO.getById(userId);
        Loan loan = loanDAO.getLoanById(loanId);

        if (inv == null || loan == null || !"funding".equals(loan.getStatus())) {
            response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?msg=invalid");
            return;
        }
        if (!"verified".equals(inv.getVerificationStatus())) {
            response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?msg=ekyc");
            return;
        }
        if (inv.getWalletBalance().compareTo(amount) < 0) {
            response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?msg=insufficient");
            return;
        }

        BigDecimal remaining = loan.getTotalAmount().subtract(
                loan.getCurrentFunded() != null ? loan.getCurrentFunded() : BigDecimal.ZERO);
        if (amount.compareTo(remaining) > 0) {
            amount = remaining;
        }

        boolean ok = investmentDAO.insertPendingInvestment(loanId, userId, amount, bankName, accountNumber);
        if (ok) {
            notifDAO.addNotification(userId, "Đăng ký góp vốn",
                    "Khoản góp vốn " + amount + " VNĐ đang chờ Admin duyệt.");
        }

        response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?msg=" + (ok ? "pending" : "fail"));
    }
}
