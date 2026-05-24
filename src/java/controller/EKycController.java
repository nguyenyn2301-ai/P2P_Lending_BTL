package controller;

import dao.UserDAO;
import util.UploadConfig;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/EKycController")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class EKycController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("re_ekyc".equals(action)) {
            request.getRequestDispatcher("ekyc.jsp").forward(request, response);
            return;
        }

        response.sendRedirect("ekyc.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UploadConfig.ensureDirectoriesExist();

        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (userId == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            Part frontPart = request.getPart("frontImg");
            Part backPart = request.getPart("backImg");
            Part facePart = request.getPart("faceImg");

            if (isEmpty(frontPart) || isEmpty(backPart) || isEmpty(facePart)) {
                response.sendRedirect("ekyc.jsp?error=missingFiles");
                return;
            }

            userDAO.saveOrUpdateDocument(userId, "id_card_front",
                    UploadConfig.saveEkycFile(frontPart, userId, "id_card_front"));
            userDAO.saveOrUpdateDocument(userId, "id_card_back",
                    UploadConfig.saveEkycFile(backPart, userId, "id_card_back"));
            userDAO.saveOrUpdateDocument(userId, "other",
                    UploadConfig.saveEkycFile(facePart, userId, "other"));

            boolean isUpdated = userDAO.updateOrInsertEkycStatus(userId, "pending");

            if (isUpdated) {
                session.setAttribute("verification_status", "pending");
            }

            if ("borrower".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/BorrowerDashboardServlet?action=dashboard&msg=ekyc_updated_success");
            } else if ("investor".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/InvestorDashboardServlet?action=profile&msg=ekyc_updated_success");
            } else {
                response.sendRedirect("index.jsp");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ekyc.jsp?error=uploadFailed");
        }
    }

    private boolean isEmpty(Part part) {
        return part == null || part.getSize() == 0
                || part.getSubmittedFileName() == null
                || part.getSubmittedFileName().isEmpty();
    }
}
