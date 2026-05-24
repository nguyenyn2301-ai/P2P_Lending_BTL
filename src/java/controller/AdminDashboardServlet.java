package controller;

import dao.*;
import model.Notification;
import model.WalletDeposit;
import util.UploadConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/AdminDashboardServlet")
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        UploadConfig.ensureDirectoriesExist();

        String section = request.getParameter("section");
        if (section == null || section.isEmpty()) section = "ekyc";

        UserDAO userDAO = new UserDAO();
        LoanDAO loanDAO = new LoanDAO();
        InvestmentDAO investmentDAO = new InvestmentDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        WalletDepositDAO walletDepositDAO = new WalletDepositDAO();

        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            adminId = userDAO.getAdminUserId();
        }
        if (adminId != null && adminId > 0) {
            List<Notification> adminNotifications = notificationDAO.getNotificationsByUserId(adminId);
            request.setAttribute("adminNotifications", adminNotifications);
        }

        try {
            switch (section) {
                case "ekyc":
                    request.setAttribute("pendingEkycList", userDAO.getPendingEkycUsers());
                    break;
                case "investments":
                    request.setAttribute("pendingInvestments", investmentDAO.getPendingInvestments());
                    break;
                case "investor_deposits":
                    List<WalletDeposit> pendingDeposits = walletDepositDAO.getPendingDeposits();
                    request.setAttribute("pendingDeposits", pendingDeposits);
                    break;
                case "loan_new":
                    request.setAttribute("pendingLoanApps", loanDAO.getPendingLoans());
                    break;
                case "loan_current":
                    request.setAttribute("currentFundingLoans", loanDAO.getFundingLoansCurrent());
                    break;
                case "loan_expired":
                    request.setAttribute("expiredLoans", loanDAO.getExpiredFundingLoans());
                    break;
                case "loan_processing":
                    request.setAttribute("processingLoans", loanDAO.getProcessingLoans());
                    request.setAttribute("overdueLoans", loanDAO.getOverdueLoans());
                    request.setAttribute("awaitingCloseLoans", loanDAO.getLoansAwaitingClosure());
                    break;
                case "loan_closed":
                    request.setAttribute("closedLoans", loanDAO.getCompletedLoans());
                    break;
                case "users":
                    String roleFilter = request.getParameter("roleFilter");
                    if (roleFilter == null) roleFilter = "all";
                    List<Map<String, Object>> users = userDAO.getAllUsersByRole(roleFilter);
                    request.setAttribute("userList", users);
                    request.setAttribute("roleFilter", roleFilter);
                    break;
                case "documents":
                    long docUserId = Long.parseLong(request.getParameter("userId"));
                    request.setAttribute("docUserId", docUserId);
                    request.setAttribute("ekycDocList", userDAO.getEkycDocumentsByUserId(docUserId));
                    request.setAttribute("loanPdfList", loanDAO.getLoanPdfDocumentsByBorrower(docUserId));
                    break;
                default:
                    section = "ekyc";
                    request.setAttribute("pendingEkycList", userDAO.getPendingEkycUsers());
            }

            request.setAttribute("currentSection", section);
            request.setAttribute("adminEmail", session.getAttribute("adminEmail"));
            request.getRequestDispatcher("/admin_dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải trang quản trị.");
        }
    }
}
