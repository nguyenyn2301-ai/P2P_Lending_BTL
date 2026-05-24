package util;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class UploadConfig {

    public static final String EKYC_PREFIX = "ekyc/";
    public static final String PDF_PREFIX = "pdf/";
    public static final String PDF_INVESTOR_PREFIX = "pdf_investor/";
    private static final String CONFIG_FILE = "upload.properties";
    private static final String DEFAULT_EKYC_DIR = "C:\\WebData\\ekyc";
    private static final String DEFAULT_PDF_DIR = "C:\\WebData\\pdf";
    private static final String DEFAULT_PDF_INVESTOR_DIR = "C:\\WebData\\pdf_investor";

    private static String ekycDir = DEFAULT_EKYC_DIR;
    private static String pdfDir = DEFAULT_PDF_DIR;
    private static String pdfInvestorDir = DEFAULT_PDF_INVESTOR_DIR;
    private static boolean initialized = false;

    private UploadConfig() {}

    public static synchronized void ensureDirectoriesExist() {
        if (!initialized) {
            loadProperties();
            initialized = true;
        }
        try {
            Files.createDirectories(Paths.get(ekycDir));
            Files.createDirectories(Paths.get(pdfDir));
            Files.createDirectories(Paths.get(pdfInvestorDir));
        } catch (IOException e) {
            System.err.println("[UploadConfig] Lỗi không thể tạo thư mục: " + e.getMessage());
        }
    }

    public static String getEkycDir() {
        ensureDirectoriesExist();
        return ekycDir;
    }

    public static String getPdfDir() {
        ensureDirectoriesExist();
        return pdfDir;
    }

    public static String getPdfInvestorDir() {
        ensureDirectoriesExist();
        return pdfInvestorDir;
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = UploadConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
                String ekyc = props.getProperty("ekyc.dir");
                String pdf = props.getProperty("pdf.dir");
                String pdfInvestor = props.getProperty("pdf_investor.dir");
                if (ekyc != null && !ekyc.trim().isEmpty()) {
                    ekycDir = ekyc.trim();
                }
                if (pdf != null && !pdf.trim().isEmpty()) {
                    pdfDir = pdf.trim();
                }
                if (pdfInvestor != null && !pdfInvestor.trim().isEmpty()) {
                    pdfInvestorDir = pdfInvestor.trim();
                }
            }
        } catch (IOException e) {
            System.err.println("[UploadConfig] Không đọc được " + CONFIG_FILE + ", dùng mặc định.");
        }
    }

    public static String saveEkycFile(Part part, long userId, String documentType) throws IOException {
        ensureDirectoriesExist();
        String ext = extensionFromPart(part, "jpg");
        String fileName = userId + "_" + documentType + "_" + System.currentTimeMillis() + "." + ext;
        Path target = Paths.get(ekycDir, fileName);
        part.write(target.toString());
        return EKYC_PREFIX + fileName;
    }

    public static String savePdfFile(Part part, long userId) throws IOException {
        ensureDirectoriesExist();
        String fileName = "loan_pdf_" + userId + "_" + System.currentTimeMillis() + ".pdf";
        Path target = Paths.get(pdfDir, fileName);
        part.write(target.toString());
        return PDF_PREFIX + fileName;
    }

    public static String saveInvestorDepositPdf(Part part, long userId) throws IOException {
        ensureDirectoriesExist();
        String fileName = "invest_deposit_" + userId + "_" + System.currentTimeMillis() + ".pdf";
        Path target = Paths.get(pdfInvestorDir, fileName);
        part.write(target.toString());
        return PDF_INVESTOR_PREFIX + fileName;
    }

    public static String toPublicUrl(String contextPath, String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) {
            return "";
        }
        String p = dbPath.trim().replace('\\', '/');
        if (p.startsWith("http://") || p.startsWith("https://")) {
            return p;
        }
        String ctx = contextPath != null ? contextPath : "";
        if (p.startsWith("uploads/")) {
            return ctx + "/" + p;
        }
        return ctx + "/uploads/" + p;
    }

    public static File resolvePhysicalFile(String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) {
            return null;
        }
        String p = dbPath.trim().replace('\\', '/');

        if (p.startsWith(EKYC_PREFIX)) {
            return new File(ekycDir, p.substring(EKYC_PREFIX.length()));
        }
        if (p.startsWith(PDF_PREFIX)) {
            return new File(pdfDir, p.substring(PDF_PREFIX.length()));
        }
        if (p.startsWith(PDF_INVESTOR_PREFIX)) {
            return new File(pdfInvestorDir, p.substring(PDF_INVESTOR_PREFIX.length()));
        }
        if (p.startsWith("uploads/")) {
            String bare = p.substring("uploads/".length());
            File inPdf = new File(pdfDir, bare);
            if (inPdf.isFile()) {
                return inPdf;
            }
            File inEkyc = new File(ekycDir, bare);
            if (inEkyc.isFile()) {
                return inEkyc;
            }
            return inPdf;
        }
        File inEkyc = new File(ekycDir, p);
        if (inEkyc.isFile()) {
            return inEkyc;
        }
        return new File(pdfDir, p);
    }

    public static void deletePhysicalFiles(java.util.List<String> dbPaths) {
        if (dbPaths == null) {
            return;
        }
        for (String dbPath : dbPaths) {
            File f = resolvePhysicalFile(dbPath);
            if (f != null && f.isFile()) {
                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    System.err.println("[UploadConfig] Không xóa được file: " + f.getAbsolutePath());
                }
            }
        }
    }

    /** Borrower/Investor upload: chỉ cần file có dữ liệu (size > 0). */
    public static boolean hasUploadedFile(Part part) {
        return part != null && part.getSize() > 0;
    }

    private static String extensionFromPart(Part part, String defaultExt) {
        String submitted = part.getSubmittedFileName();
        if (submitted != null && submitted.contains(".")) {
            return submitted.substring(submitted.lastIndexOf('.') + 1).toLowerCase();
        }
        String ct = part.getContentType();
        if (ct != null) {
            if (ct.contains("png")) return "png";
            if (ct.contains("jpeg") || ct.contains("jpg")) return "jpg";
            if (ct.contains("pdf")) return "pdf";
        }
        return defaultExt;
    }
}