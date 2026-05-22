package controller;

import dao.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/AdminActionServlet")
public class AdminActionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/admin_login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String ctx = request.getContextPath();
        UserDAO userDAO = new UserDAO();
        LoanDAO loanDAO = new LoanDAO();
        NotificationDAO notifDAO = new NotificationDAO();

        try {
            if ("approve_ekyc".equals(action)) {
                long userId = Long.parseLong(request.getParameter("userId"));
                userDAO.updateUserVerification(userId, "verified");
                notifDAO.addNotification(userId, "eKYC được duyệt", "Hồ sơ định danh của bạn đã được Admin phê duyệt. Tài khoản đã kích hoạt.");
                response.sendRedirect(ctx + "/AdminDashboardServlet?section=ekyc&msg=ekyc_ok");
            } else if ("reject_ekyc".equals(action)) {
                long userId = Long.parseLong(request.getParameter("userId"));
                userDAO.updateUserVerification(userId, "rejected");
                notifDAO.addNotification(userId, "eKYC bị từ chối", "Hồ sơ định danh không đạt yêu cầu. Vui lòng cập nhật lại.");
                response.sendRedirect(ctx + "/AdminDashboardServlet?section=ekyc&msg=ekyc_reject");
            } else if ("approve_investment".equals(action)) {
                long investmentId = Long.parseLong(request.getParameter("investmentId"));
                if (loanDAO.approvePendingInvestment(investmentId)) {
                    response.sendRedirect(ctx + "/AdminDashboardServlet?section=investments&msg=inv_ok");
                } else {
                    response.sendRedirect(ctx + "/AdminDashboardServlet?section=investments&msg=inv_fail");
                }
            } else if ("approve_loan".equals(action)) {
                long appId = Long.parseLong(request.getParameter("applicationId"));
                double rate = 12.0;
                try {
                    rate = Double.parseDouble(request.getParameter("interestRate"));
                } catch (Exception ignored) {}
                loanDAO.updateLoanStatus(appId, "approved", rate);
                response.sendRedirect(ctx + "/AdminDashboardServlet?section=loan_new&msg=loan_ok");
            } else if ("reject_loan".equals(action)) {
                long appId = Long.parseLong(request.getParameter("applicationId"));
                loanDAO.updateLoanStatus(appId, "rejected", 0);
                response.sendRedirect(ctx + "/AdminDashboardServlet?section=loan_new&msg=loan_reject");
            } else if ("disburse".equals(action)) {
                long loanId = Long.parseLong(request.getParameter("loanId"));
                if (loanDAO.disburseLoan(loanId)) {
                    response.sendRedirect(ctx + "/AdminDashboardServlet?section=loan_processing&msg=disburse_ok");
                } else {
                    response.sendRedirect(ctx + "/AdminDashboardServlet?section=loan_processing&msg=disburse_fail");
                }
            } else if ("send_overdue_notice".equals(action)) {
                long loanId = Long.parseLong(request.getParameter("loanId"));
                model.Loan loan = loanDAO.getLoanById(loanId);
                if (loan != null) {
                    String title = "Cảnh báo quá hạn nghiêm trọng";
                    String msg = "Gói vay " + (loan.getLoanCode() != null ? loan.getLoanCode() : "#" + loanId)
                            + " đã quá hạn thanh toán. Vui lòng liên hệ ngay!";
                    notifDAO.addNotification(loan.getBorrowerId(), title, msg);
                    InvestmentDAO invDao = new InvestmentDAO();
                    for (model.Investment inv : invDao.getByLoanId(loanId)) {
                        notifDAO.addNotification(inv.getInvestorId(), title, msg);
                    }
                }
                response.sendRedirect(ctx + "/AdminDashboardServlet?section=loan_processing&msg=notice_ok");
            } else if ("close_loan".equals(action)) {
                long loanId = Long.parseLong(request.getParameter("loanId"));
                loanDAO.updateLoanStatusById(loanId, "completed");
                model.Loan loan = loanDAO.getLoanById(loanId);
                if (loan != null) {
                    notifDAO.addNotification(loan.getBorrowerId(), "Gói vay đã kết thúc",
                            "Admin đã duyệt kết thúc gói vay. Bạn có thể đăng ký vay mới.");
                }
                response.sendRedirect(ctx + "/AdminDashboardServlet?section=loan_closed&msg=close_ok");
            } else {
                response.sendRedirect(ctx + "/AdminDashboardServlet");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(ctx + "/AdminDashboardServlet?msg=error");
        }
    }
}
