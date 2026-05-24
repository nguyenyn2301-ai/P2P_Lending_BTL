package controller;

import dao.BorrowerDAO;
import dao.LoanDAO;
import dao.NotificationDAO;
import dao.UserDAO;
import model.Notification;
import model.LoanApplication;
import model.Borrower;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig; // 1. Đã thêm import cho Multipart
import jakarta.servlet.http.*;
import jakarta.servlet.http.Part;
import util.UploadConfig;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/BorrowerDashboardServlet")
// BƯỚC 1: Thêm cấu hình MultipartConfig để Servlet hiểu được dữ liệu file gửi lên từ Form
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB (Giới hạn tối đa 1 file PDF)
    maxRequestSize = 1024 * 1024 * 50     // 50MB (Tổng dung lượng request)
)
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

            // Đồng bộ trạng thái eKYC từ DB (chuỗi tiếng Anh: pending, verified, rejected)
            UserDAO userDAO = new UserDAO();
            String verificationStatus = userDAO.getEkycStatus(userId);
            if (verificationStatus == null || verificationStatus.trim().isEmpty()) {
                verificationStatus = "none";
            }
            session.setAttribute("verification_status", verificationStatus);

            List<LoanApplication> loanList = bDao.getLoansByBorrower(userId);
            boolean hasActiveLoan = bDao.hasUnresolvedCapitalPackage(userId);
            boolean hasOverdue = false;
            for (model.Loan active : lDao.getActiveLoansByBorrower(userId)) {
                if ("overdue".equals(active.getStatus())) hasOverdue = true;
            }
            if (hasOverdue) {
                hasActiveLoan = true;
            }

            if ("create_loan".equals(currentAction) && hasActiveLoan) {
                response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=dashboard&msg=error_already_has_loan");
                return;
            }

            List<Notification> notifications = new NotificationDAO().getNotificationsByUserId(userId);

            if ("re_ekyc".equals(currentAction)) {
                request.setAttribute("borrowerObj", borrower);
            } else if ("market_loans".equals(currentAction)) {
                List<LoanApplication> marketLoans = lDao.getAllMarketLoans(); 
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
        
        // Cấu hình encoding nhận dữ liệu text từ form tránh lỗi font tiếng Việt
        request.setCharacterEncoding("UTF-8");
        
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
            // XỬ LÝ: Đơn đăng ký vay mới kèm upload file PDF
            if ("submit_loan".equals(action)) {
                UploadConfig.ensureDirectoriesExist();

                String amountStr = request.getParameter("amountRequested");
                String termStr = request.getParameter("termMonths");
                String cicIssuedDateStr = request.getParameter("cicIssuedDate");

                Part filePart = request.getPart("cicPdfUrl");
                String dbFilePath;

                if (!UploadConfig.hasUploadedFile(filePart)) {
                    response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=create_loan&msg=loan_pdf_required");
                    return;
                }

                dbFilePath = UploadConfig.savePdfFile(filePart, userId);

                // Chuyển đổi an toàn sang BigDecimal để đồng bộ với cấu trúc Model mới
                BigDecimal amountRequested = BigDecimal.ZERO;
                if (amountStr != null && !amountStr.isEmpty()) {
                    amountRequested = new BigDecimal(amountStr);
                }
                
                int termMonths = (termStr != null && !termStr.isEmpty()) ? Integer.parseInt(termStr) : 0;

                if (bDao.hasUnresolvedCapitalPackage(userId)) {
                    response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=dashboard&msg=error_already_has_loan");
                    return;
                }

                // Khởi tạo đối tượng lưu trữ theo chuẩn thiết kế mới
                LoanApplication newLoan = new LoanApplication();
                newLoan.setBorrowerId(userId);
                newLoan.setAmountRequested(amountRequested);
                newLoan.setTermMonths(termMonths);
                newLoan.setStatus("pending"); // Đồng bộ chuỗi trạng thái gốc lưu DB tiếng Anh viết thường
                
                // ĐÂY RỒI: Gán đường dẫn file vật lý vừa lưu thành công thay vì lấy chuỗi text thuần từ parameter
                newLoan.setCicPdfUrl(dbFilePath);

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

                // Thực thi chèn dữ liệu vào DB thông qua LoanDAO
                boolean success = lDao.insertLoanApplication(newLoan);
                
                if (success) {
                    response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=dashboard&msg=loan_submit_success");
                } else {
                    response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=create_loan&msg=loan_submit_failed");
                }
                return;
            } else if ("update_ekyc".equals(action)) {
                UploadConfig.ensureDirectoriesExist();
                UserDAO userDAO = new UserDAO();

                String firstName = request.getParameter("firstName");
                String lastName = request.getParameter("lastName");
                String idCardNumber = request.getParameter("idCardNumber");
                String monthlyIncomeStr = request.getParameter("monthlyIncome");

                double monthlyIncome = 0.0;
                if (monthlyIncomeStr != null && !monthlyIncomeStr.trim().isEmpty()) {
                    monthlyIncome = Double.parseDouble(monthlyIncomeStr.trim());
                }

                if (firstName != null && lastName != null && idCardNumber != null) {
                    bDao.updateBorrowerProfile(userId, firstName.trim(), lastName.trim(),
                            idCardNumber.trim(), monthlyIncome);
                }

                Part frontPart = request.getPart("cccd_front");
                Part backPart = request.getPart("cccd_back");
                Part facePart = request.getPart("selfie_avatar");

                if (isEmptyPart(frontPart) || isEmptyPart(backPart) || isEmptyPart(facePart)) {
                    response.sendRedirect(request.getContextPath()
                            + "/BorrowerDashboardServlet?action=re_ekyc&msg=ekyc_updated_failed");
                    return;
                }

                userDAO.saveOrUpdateDocument(userId, "id_card_front",
                        UploadConfig.saveEkycFile(frontPart, userId, "id_card_front"));
                userDAO.saveOrUpdateDocument(userId, "id_card_back",
                        UploadConfig.saveEkycFile(backPart, userId, "id_card_back"));
                userDAO.saveOrUpdateDocument(userId, "other",
                        UploadConfig.saveEkycFile(facePart, userId, "other"));

                if (userDAO.updateOrInsertEkycStatus(userId, "pending")) {
                    session.setAttribute("verification_status", "pending");
                    response.sendRedirect(request.getContextPath()
                            + "/BorrowerDashboardServlet?action=dashboard&msg=ekyc_updated_success");
                } else {
                    response.sendRedirect(request.getContextPath()
                            + "/BorrowerDashboardServlet?action=re_ekyc&msg=ekyc_updated_failed");
                }
                return;
            }

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

    private boolean isEmptyPart(Part part) {
        return part == null || part.getSize() == 0
                || part.getSubmittedFileName() == null
                || part.getSubmittedFileName().isEmpty();
    }

}