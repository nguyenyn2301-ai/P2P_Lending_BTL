package controller;

import dao.InvestmentDAO;
import dao.LoanDAO;
import model.Investor;
import model.Loan;
import model.Investment;
import util.NumberToVietnameseWords;
import dao.InvestorDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebServlet("/ContractServlet")
public class ContractServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long loanId;
        try {
            loanId = Long.parseLong(request.getParameter("loanId"));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã gói vay.");
            return;
        }

        LoanDAO loanDAO = new LoanDAO();
        InvestmentDAO investmentDAO = new InvestmentDAO();
        InvestorDAO investorDAO = new InvestorDAO();

        Loan loan = loanDAO.getLoanById(loanId);
        if (loan == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        java.util.List<Investment> investments = investmentDAO.getByLoanId(loanId);
        String investorName = "Bên cho vay P2P";
        if (!investments.isEmpty()) {
            Investor inv = investorDAO.getById(investments.get(0).getInvestorId());
            if (inv != null) investorName = inv.getFullName();
        }

        request.setAttribute("loan", loan);
        request.setAttribute("borrowerName", loan.getBorrowerName());
        request.setAttribute("investorName", investorName);
        request.setAttribute("amountWords", NumberToVietnameseWords.convert(loan.getTotalAmount()));
        request.setAttribute("disburseDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        request.setAttribute("contractCode", loan.getLoanCode() != null ? loan.getLoanCode() : "GV-" + loanId);

        request.getRequestDispatcher("/contract.jsp").forward(request, response);
    }
}
