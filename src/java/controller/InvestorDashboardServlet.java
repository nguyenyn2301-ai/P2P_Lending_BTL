package controller;

import dao.*;
import model.Investment;
import model.Investor;
import model.LoanApplication;
import model.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import util.UploadConfig;
import java.util.Date;
import java.util.List;
import jakarta.servlet.http.Part;

@WebServlet("/InvestorDashboardServlet")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 15 * 1024 * 1024)
public class InvestorDashboardServlet extends HttpServlet {

    private static final String ADMIN_BANK_ACCOUNT = "868396202";
    private static final String ADMIN_BANK_NAME = "VietComBank";

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

        String currentAction = request.getParameter("action");
        if (currentAction == null || currentAction.isEmpty()) {
            currentAction = "dashboard";
        }

        if ("re_ekyc".equals(currentAction)) {
            request.getRequestDispatcher("ekyc.jsp").forward(request, response);
            return;
        }

        InvestorDAO investorDAO = new InvestorDAO();
        LoanDAO loanDAO = new LoanDAO();
        InvestmentDAO investmentDAO = new InvestmentDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        UserDAO userDAO = new UserDAO();

        Investor investor = investorDAO.getById(userId);
        String verificationStatus = userDAO.getEkycStatus(userId);
        if (verificationStatus == null || verificationStatus.trim().isEmpty()) {
            verificationStatus = "none";
        }
        session.setAttribute("verification_status", verificationStatus);

        List<LoanApplication> fundingLoans = null;
        if ("market".equals(currentAction)) {
            fundingLoans = loanDAO.getAllMarketLoans();
        }

        if ("deposit".equals(currentAction) && investor != null) {
            String transferContent = buildTransferContent(investor.getFullName().trim());
            request.setAttribute("transferContent", transferContent);
            request.setAttribute("adminBankAccount", ADMIN_BANK_ACCOUNT);
            request.setAttribute("adminBankName", ADMIN_BANK_NAME);
        }

        List<Investment> myInvestments = investmentDAO.getInvestmentsByInvestor(userId);
        List<Notification> notifications = notificationDAO.getNotificationsByUserId(userId);

        request.setAttribute("currentAction", currentAction);
        request.setAttribute("trangThaiEkyc", verificationStatus);
        request.setAttribute("investor", investor);
        request.setAttribute("investorName", investor != null ? investor.getFullName() : "Nhà đầu tư");
        request.setAttribute("fundingLoans", fundingLoans);
        request.setAttribute("myInvestments", myInvestments);
        request.setAttribute("notifications", notifications);
        request.getRequestDispatcher("/investor_dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Long userId = session != null ? (Long) session.getAttribute("userId") : null;
        String role = session != null ? (String) session.getAttribute("role") : null;
        String ctx = request.getContextPath();

        if (userId == null || !"investor".equals(role)) {
            response.sendRedirect(ctx + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (!"submit_deposit".equals(action)) {
            response.sendRedirect(ctx + "/InvestorDashboardServlet?action=dashboard");
            return;
        }

        try {
            UploadConfig.ensureDirectoriesExist();

            String amountStr = request.getParameter("depositAmount");
            if (amountStr == null || amountStr.trim().isEmpty()) {
                response.sendRedirect(ctx + "/InvestorDashboardServlet?action=deposit&msg=deposit_invalid");
                return;
            }

            BigDecimal amount = new BigDecimal(amountStr.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                response.sendRedirect(ctx + "/InvestorDashboardServlet?action=deposit&msg=deposit_invalid");
                return;
            }
            Part pdfPart = request.getPart("cicPdfUrl");
            if (!UploadConfig.hasUploadedFile(pdfPart)) {
                response.sendRedirect(ctx + "/InvestorDashboardServlet?action=deposit&msg=deposit_pdf_required");
                return;
            }

            InvestorDAO investorDAO = new InvestorDAO();
            Investor investor = investorDAO.getById(userId);
            if (investor == null) {
                response.sendRedirect(ctx + "/InvestorDashboardServlet?action=deposit&msg=deposit_fail");
                return;
            }

            String transferContent = buildTransferContent(investor.getFullName().trim());
            String dbPdfPath = UploadConfig.saveInvestorDepositPdf(pdfPart, userId);

            WalletDepositDAO depositDAO = new WalletDepositDAO();
            long depositId = depositDAO.insertDeposit(userId, amount, transferContent, dbPdfPath);
            if (depositId <= 0) {
                response.sendRedirect(ctx + "/InvestorDashboardServlet?action=deposit&msg=deposit_fail");
                return;
            }

            UserDAO userDAO = new UserDAO();
            long adminId = userDAO.getAdminUserId();
            if (adminId > 0) {
                String amountDisplay = amount.stripTrailingZeros().toPlainString();
                String adminMsg = transferContent + " " + amountDisplay;
                NotificationDAO notifDAO = new NotificationDAO();
                notifDAO.addNotification(adminId, "Yêu cầu nạp tiền nhà đầu tư", adminMsg, dbPdfPath);
            }

            response.sendRedirect(ctx + "/InvestorDashboardServlet?action=dashboard&msg=deposit_pending");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(ctx + "/InvestorDashboardServlet?action=deposit&msg=deposit_fail");
        }
    }

    static String buildTransferContent(String fullName) {
        String datePart = new SimpleDateFormat("ddMMyyyy").format(new Date());
        return fullName + "+invest+" + datePart + "+P2p_lending";
    }

}
