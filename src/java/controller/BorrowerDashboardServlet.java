package controller;

import dao.BorrowerDAO;
import dao.LoanDAO;
import dao.NotificationDAO;
import model.Notification;
import model.LoanApplication;
import model.Borrower;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/BorrowerDashboardServlet")
public class BorrowerDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String currentAction = request.getParameter("action");
        if (currentAction == null || currentAction.isEmpty()) {
            currentAction = "dashboard";
        }

        try {
            BorrowerDAO bDao = new BorrowerDAO();
            LoanDAO lDao = new LoanDAO();
            
            Borrower borrower = bDao.getBorrowerById(userId);
            
            // Cơ chế fallback an toàn nếu không tìm thấy dữ liệu user test
            if (borrower == null) {
                borrower = bDao.getBorrowerById(1L);
                if (borrower != null) {
                    userId = borrower.getBorrowerId();
                }
            }

            String fullName = "Người dùng";
            double monthlyIncome = 0.0;
            double maxLimit = 0.0;

            if (borrower != null) {
                fullName = borrower.getFirstName() + " " + borrower.getLastName();
                monthlyIncome = borrower.getMonthlyIncome();
                maxLimit = monthlyIncome * 3.0; 
            }

            // Đồng bộ trạng thái xác thực eKYC giữa Database và Session
            String verificationStatus = (String) session.getAttribute("verification_status");
            if (verificationStatus == null || "none".equals(verificationStatus)) {
                if (borrower != null && borrower.getVerificationStatus() != null) {
                    verificationStatus = borrower.getVerificationStatus();
                } else {
                    verificationStatus = "Chờ duyệt";
                }
                session.setAttribute("verification_status", verificationStatus);
            }

            List<LoanApplication> loanList = bDao.getLoansByBorrower(userId);
            boolean hasActiveLoan = false;
            if (loanList != null) {
                for (LoanApplication loan : loanList) {
                    String status = loan.getStatus();
                    if ("Chờ duyệt".equals(status) || "Đã duyệt".equals(status) || "Đang gọi vốn".equals(status)) {
                        hasActiveLoan = true;
                        break;
                    }
                }
            }
            if (!lDao.getActiveLoansByBorrower(userId).isEmpty()) {
                hasActiveLoan = true;
            }
            boolean hasOverdue = false;
            for (model.Loan active : lDao.getActiveLoansByBorrower(userId)) {
                if ("overdue".equals(active.getStatus())) hasOverdue = true;
            }

            List<Notification> notifications = new NotificationDAO().getNotificationsByUserId(userId);

            // HÀNH ĐỘNG 1: Tái xác thực eKYC
            if ("re_ekyc".equals(currentAction)) {
                request.setAttribute("borrowerObj", borrower);
                request.getRequestDispatcher("ekyc.jsp").forward(request, response);
                return;
            } 
            // HÀNH ĐỘNG 2: Vào Chợ Gọi Vốn Toàn Sàn
            else if ("market_loans".equals(currentAction)) {
                // Đã tối ưu: Lấy dữ liệu JOIN chính xác, không dùng vòng lặp ghi đè cứng thông tin nữa
                List<LoanApplication> marketLoans = lDao.getAllMarketLoans(); 
                
                // Đẩy danh sách đã xử lý an toàn bảo mật sang cho JSP render
                request.setAttribute("marketLoansList", marketLoans);
            }

            // Tính tổng dư nợ thực tế từ DB thông qua BigDecimal chuyển đổi
            double currentDebt = bDao.getCurrentDebt(userId);

            // Đẩy toàn bộ dữ liệu sạch ra vùng hiển thị của file JSP
            request.setAttribute("currentAction", currentAction);
            request.setAttribute("borrowerName", fullName);
            request.setAttribute("trangThaiEkyc", verificationStatus);
            request.setAttribute("thuNhapKhai", monthlyIncome);
            request.setAttribute("hanMucToiDa", maxLimit);
            request.setAttribute("tongDuNo", currentDebt);
            request.setAttribute("myLoansList", loanList);
            request.setAttribute("hasActiveLoan", hasActiveLoan || hasOverdue);
            request.setAttribute("hasOverdue", hasOverdue);
            request.setAttribute("notifications", notifications);

            request.getRequestDispatcher("borrower_dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Hệ thống gặp sự cố khi tải bảng điều khiển.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        BorrowerDAO bDao = new BorrowerDAO();
        LoanDAO lDao = new LoanDAO();

        try {
            // XỬ LÝ: Đơn đăng ký vay mới từ form borrower_dashboard.jsp
            if ("submit_loan".equals(action)) {
                String amountStr = request.getParameter("amountRequested");
                String termStr = request.getParameter("termMonths");
                String cicIssuedDateStr = request.getParameter("cicIssuedDate");
                String cicPdfUrl = request.getParameter("cicPdfUrl");

                // Chuyển đổi an toàn sang BigDecimal để đồng bộ với cấu trúc Model mới
                BigDecimal amountRequested = BigDecimal.ZERO;
                if (amountStr != null && !amountStr.isEmpty()) {
                    amountRequested = new BigDecimal(amountStr);
                }
                
                int termMonths = (termStr != null && !termStr.isEmpty()) ? Integer.parseInt(termStr) : 0;

                if (cicPdfUrl != null && cicPdfUrl.trim().isEmpty()) {
                    cicPdfUrl = null;
                }

                List<LoanApplication> loanList = bDao.getLoansByBorrower(userId);
                boolean hasActiveLoan = false;
                if (loanList != null) {
                    for (LoanApplication loan : loanList) {
                        String status = loan.getStatus();
                        if ("Chờ duyệt".equals(status) || "Đã duyệt".equals(status) || "Đang gọi vốn".equals(status)) {
                            hasActiveLoan = true;
                            break;
                        }
                    }
                }

                // Chặn không cho vay thêm nếu đang có đơn vay chưa tất toán
                if (hasActiveLoan) {
                    response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=dashboard&msg=error_already_has_loan");
                    return;
                }

                // Khởi tạo đối tượng lưu trữ theo chuẩn thiết kế mới
                LoanApplication newLoan = new LoanApplication();
                newLoan.setBorrowerId(userId);
                newLoan.setAmountRequested(amountRequested);
                newLoan.setTermMonths(termMonths);
                newLoan.setStatus("pending"); // Đồng bộ chuỗi trạng thái gốc lưu DB tiếng Anh viết thường
                newLoan.setCicPdfUrl(cicPdfUrl);

                if (cicIssuedDateStr != null && !cicIssuedDateStr.isEmpty()) {
                    try {
                        newLoan.setCicIssuedDate(Date.valueOf(cicIssuedDateStr));
                    } catch (IllegalArgumentException e) {
                        newLoan.setCicIssuedDate(new Date(System.currentTimeMillis()));
                    }
                } else {
                    newLoan.setCicIssuedDate(new Date(System.currentTimeMillis()));
                }
                
                newLoan.setCreatedAt(new Timestamp(System.currentTimeMillis()));

                // Thực thi chèn dữ liệu
                boolean success = lDao.insertLoanApplication(newLoan);
                
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=dashboard&msg=loan_submit_success");
                } else {
                    response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=create_loan&msg=loan_submit_failed");
                }
                return;
            } 
            
            // Mặc định chuyển hướng an toàn về màn hình chính nếu action không khớp
            response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html;charset=UTF-8");
            try (java.io.PrintWriter out = response.getWriter()) {
                out.println("<div style='padding:20px; border:1px solid #ef4444; background:#fef2f2; color:#991b1b; font-family:sans-serif;'>");
                out.println("<h2>💥 Đã xảy ra lỗi hệ thống xử lý Backend (Servlet):</h2>");
                out.println("<pre style='background:#ffffff; padding:15px; border-radius:6px; border:1px solid #fca5a5; overflow-x:auto;'>");
                e.printStackTrace(out);
                out.println("</pre>");
                out.println("</div>");
            }
        }
    }
}