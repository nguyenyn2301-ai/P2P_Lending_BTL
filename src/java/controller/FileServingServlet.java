package controller;

import util.UploadConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
@WebServlet(urlPatterns = {"/uploads/*"})
public class FileServingServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        UploadConfig.ensureDirectoriesExist();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1 || pathInfo.contains("..")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String relativePath = pathInfo.substring(1).replace('\\', '/');
        File file = UploadConfig.resolvePhysicalFile(relativePath);

        if (file == null || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String canonicalRootEkyc = new File(UploadConfig.getEkycDir()).getCanonicalPath();
        String canonicalRootPdf = new File(UploadConfig.getPdfDir()).getCanonicalPath();
        String canonicalRootPdfInvestor = new File(UploadConfig.getPdfInvestorDir()).getCanonicalPath();
        String canonicalFile = file.getCanonicalPath();
        if (!canonicalFile.startsWith(canonicalRootEkyc)
                && !canonicalFile.startsWith(canonicalRootPdf)
                && !canonicalFile.startsWith(canonicalRootPdfInvestor)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String mime = getServletContext().getMimeType(file.getName());
        if (mime == null) {
            String lower = file.getName().toLowerCase();
            if (lower.endsWith(".pdf")) {
                mime = "application/pdf";
            } else if (lower.endsWith(".png")) {
                mime = "image/png";
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                mime = "image/jpeg";
            } else {
                mime = "application/octet-stream";
            }
        }

        response.setContentType(mime);
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        response.setContentLengthLong(file.length());
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
