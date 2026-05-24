package controller;

import dao.*;
import model.Loan;
import model.LoanApplication;
import model.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/RepaymentServlet")
public class RepaymentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Long userId = session != null ? (Long) session.getAttribute("userId") : null;
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        LoanDAO loanDAO = new LoanDAO();
        BorrowerDAO borrowerDAO = new BorrowerDAO();
        NotificationDAO notifDAO = new NotificationDAO();

        List<Loan> loans = loanDAO.getActiveLoansByBorrower(userId);
        List<Map<String, Object>> repaymentViews = new ArrayList<>();
        boolean hasOverdue = false;
        boolean hasActiveContract = false;
        LocalDate today = LocalDate.now();

        for (Loan loan : loans) {
            hasActiveContract = true;
            Map<String, Object> view = new HashMap<>();
            view.put("loan", loan);

            String displayStatus = "processing";
            BigDecimal installment = calcInstallment(loan);
            BigDecimal penalty = BigDecimal.ZERO;
            BigDecimal totalDue = installment;

            if (loan.getDueDate() != null) {
                LocalDate due = loan.getDueDate().toLocalDate();
                if (today.isAfter(due) && loan.getPaidPeriods() < loan.getTermMonths()) {
                    displayStatus = "overdue";
                    hasOverdue = true;
                    long daysLate = ChronoUnit.DAYS.between(due, today);
                    penalty = calcPenalty(loan, daysLate);
                    totalDue = installment.add(penalty);
                    if (!"overdue".equals(loan.getStatus())) {
                        loanDAO.updateLoanStatusById(loan.getLoanId(), "overdue");
                    }
                }
            }

            if (loan.isBorrowerConfirmed()) {
                displayStatus = "completed";
            }

            view.put("displayStatus", displayStatus);
            view.put("installment", installment);
            view.put("penalty", penalty);
            view.put("totalDue", totalDue);
            view.put("isFinalPeriod", loan.getPaidPeriods() + 1 >= loan.getTermMonths());
            repaymentViews.add(view);
        }

        model.Borrower borrower = borrowerDAO.getBorrowerById(userId);
        String borrowerName = borrower != null
                ? borrower.getFirstName() + " " + borrower.getLastName()
                : "Người vay";

        UserDAO userDAO = new UserDAO();
        String verificationStatus = userDAO.getEkycStatus(userId);
        if (verificationStatus == null || verificationStatus.trim().isEmpty()) {
            verificationStatus = "none";
        }
        session.setAttribute("verification_status", verificationStatus);

        boolean hasActiveLoan = borrowerDAO.hasUnresolvedCapitalPackage(userId);

        request.setAttribute("currentAction", "repayment");
        request.setAttribute("repaymentViews", repaymentViews);
        request.setAttribute("hasOverdue", hasOverdue);
        request.setAttribute("hasActiveContract", hasActiveContract);
        request.setAttribute("hasActiveLoan", hasActiveLoan || hasOverdue);
        request.setAttribute("trangThaiEkyc", verificationStatus);
        request.setAttribute("notifications", notifDAO.getNotificationsByUserId(userId));
        request.setAttribute("borrowerName", borrowerName);
        request.getRequestDispatcher("/repayment.jsp").forward(request, response);
    }

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
        LoanDAO loanDAO = new LoanDAO();
        BorrowerDAO borrowerDAO = new BorrowerDAO();
        InvestorDAO investorDAO = new InvestorDAO();
        InvestmentDAO investmentDAO = new InvestmentDAO();
        TransactionDAO txDao = new TransactionDAO();
        NotificationDAO notifDAO = new NotificationDAO();

        Loan loan = loanDAO.getLoanById(loanId);
        if (loan == null || loan.getBorrowerId() != userId) {
            response.sendRedirect(request.getContextPath() + "/RepaymentServlet?msg=invalid");
            return;
        }

        BigDecimal installment = calcInstallment(loan);
        BigDecimal penalty = BigDecimal.ZERO;
        if (loan.getDueDate() != null) {
            LocalDate due = loan.getDueDate().toLocalDate();
            if (LocalDate.now().isAfter(due)) {
                long daysLate = ChronoUnit.DAYS.between(due, LocalDate.now());
                penalty = calcPenalty(loan, daysLate);
            }
        }
        BigDecimal totalDue = installment.add(penalty);

        model.Borrower borrower = borrowerDAO.getBorrowerById(userId);
        if (borrower == null || BigDecimal.valueOf(borrower.getMonthlyIncome()).compareTo(BigDecimal.ZERO) < 0) {
            // use wallet from DB
        }

        String walletSql = "SELECT wallet_balance FROM borrowers WHERE borrower_id = ?";
        BigDecimal wallet = BigDecimal.ZERO;
        try (var conn = util.DBConnection.getConnection();
             var ps = conn.prepareStatement(walletSql)) {
            ps.setLong(1, userId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) wallet = rs.getBigDecimal("wallet_balance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (wallet.compareTo(totalDue) < 0) {
            response.sendRedirect(request.getContextPath() + "/RepaymentServlet?msg=insufficient");
            return;
        }

        try (var conn = util.DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            String deduct = "UPDATE borrowers SET wallet_balance = wallet_balance - ? WHERE borrower_id = ?";
            try (var ps = conn.prepareStatement(deduct)) {
                ps.setBigDecimal(1, totalDue);
                ps.setLong(2, userId);
                ps.executeUpdate();
            }

            List<model.Investment> investments = investmentDAO.getByLoanId(loanId);
            BigDecimal totalInvested = BigDecimal.ZERO;
            for (model.Investment inv : investments) {
                totalInvested = totalInvested.add(inv.getAmountInvested());
            }
            for (model.Investment inv : investments) {
                BigDecimal share = totalInvested.compareTo(BigDecimal.ZERO) > 0
                        ? totalDue.multiply(inv.getAmountInvested()).divide(totalInvested, 2, RoundingMode.HALF_UP)
                        : totalDue;
                String addInv = "UPDATE investors SET wallet_balance = wallet_balance + ? WHERE investor_id = ?";
                try (var ps = conn.prepareStatement(addInv)) {
                    ps.setBigDecimal(1, share);
                    ps.setLong(2, inv.getInvestorId());
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/RepaymentServlet?msg=fail");
            return;
        }

        txDao.insertTransaction(userId, totalDue, "repayment", "completed");

        int newPaid = loan.getPaidPeriods() + 1;
        boolean isFinal = newPaid >= loan.getTermMonths();
        String newStatus = isFinal ? "process" : "process";
        Date nextDue = isFinal ? null : Date.valueOf(LocalDate.now().plusMonths(1));

        if (isFinal) {
            loanDAO.advanceRepaymentPeriod(loanId, newPaid, null, "process");
            String flagSql = "UPDATE loans SET borrower_confirmed = TRUE, status = 'process' WHERE loan_id = ?";
            try (var conn = util.DBConnection.getConnection();
                 var ps = conn.prepareStatement(flagSql)) {
                ps.setLong(1, loanId);
                ps.executeUpdate();
            } catch (Exception e) { e.printStackTrace(); }
            notifDAO.addNotification(userId, "Tất toán kỳ cuối",
                    "Bạn đã thanh toán xong kỳ cuối. Chờ Admin duyệt kết thúc gói vay.");
        } else {
            loanDAO.advanceRepaymentPeriod(loanId, newPaid, nextDue, newStatus);
            notifDAO.addNotification(userId, "Thanh toán kỳ thành công",
                    "Đã thanh toán kỳ " + newPaid + "/" + loan.getTermMonths());
        }

        response.sendRedirect(request.getContextPath() + "/RepaymentServlet?msg=ok");
    }

    private BigDecimal calcInstallment(Loan loan) {
        BigDecimal principal = loan.getTotalAmount();
        int term = loan.getTermMonths() > 0 ? loan.getTermMonths() : 1;
        BigDecimal rate = loan.getInterestRate() != null ? loan.getInterestRate() : BigDecimal.ZERO;
        BigDecimal monthlyInterest = principal.multiply(rate).divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
        return principal.divide(new BigDecimal(term), 2, RoundingMode.HALF_UP).add(monthlyInterest);
    }

    private BigDecimal calcPenalty(Loan loan, long daysLate) {
        BigDecimal rate = loan.getInterestRate() != null ? loan.getInterestRate() : BigDecimal.ZERO;
        BigDecimal dailyInTerm = loan.getTotalAmount().multiply(rate)
                .divide(new BigDecimal("36500"), 6, RoundingMode.HALF_UP);
        BigDecimal dailyPenalty = dailyInTerm.multiply(new BigDecimal("1.5"));
        return dailyPenalty.multiply(new BigDecimal(daysLate)).setScale(2, RoundingMode.HALF_UP);
    }
}
