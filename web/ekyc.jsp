<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ đăng ký (eKYC) - P2P Lending</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f7f6;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }
        .ekyc-container {
            width: 100%;
            max-width: 480px;
            margin: 20px;
            padding: 30px;
            border: 1px solid #ddd;
            border-radius: 12px;
            background: #fff;
            box-shadow: 0 8px 24px rgba(0,0,0,0.1);
        }
        h2 { text-align: center; color: #333; margin-bottom: 8px; }
        .subtitle { text-align: center; color: #666; font-size: 14px; margin-bottom: 24px; line-height: 1.5; }
        .step-badge {
            display: block;
            text-align: center;
            font-size: 13px;
            color: #007bff;
            font-weight: 600;
            margin-bottom: 16px;
        }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; font-weight: 600; margin-bottom: 8px; color: #555; }
        .form-group input[type="file"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 6px;
            box-sizing: border-box;
        }
        .btn-upload {
            background: #007bff;
            color: white;
            padding: 12px 20px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            width: 100%;
            font-size: 16px;
            font-weight: bold;
        }
        .btn-upload:hover { background: #0056b3; }
        .error-msg {
            background-color: #f8d7da;
            color: #721c24;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 16px;
            text-align: center;
            font-size: 14px;
            border: 1px solid #f5c6cb;
        }
    </style>
</head>
<body>
    <div class="ekyc-container">
        <span class="step-badge">Bước 2 / 2 — Hoàn tất hồ sơ đăng ký lần đầu</span>
        <h2>Hồ sơ đăng ký (eKYC)</h2>
        <p class="subtitle">Tài khoản đã được tạo. Vui lòng tải lên ảnh CCCD và ảnh chân dung để hoàn tất hồ sơ đăng ký và được phê duyệt quyền vay/đầu tư.</p>

        <% if ("missingFiles".equals(request.getParameter("error"))) { %>
            <div class="error-msg"><i class="fa-solid fa-circle-exclamation"></i> Vui lòng chọn đủ 3 ảnh bắt buộc.</div>
        <% } else if ("uploadFailed".equals(request.getParameter("error"))) { %>
            <div class="error-msg"><i class="fa-solid fa-circle-exclamation"></i> Upload thất bại. Vui lòng thử lại.</div>
        <% } %>

        <form action="EKycController" method="POST" enctype="multipart/form-data">
            <div class="form-group">
                <label>Mặt trước CCCD:</label>
                <input type="file" name="frontImg" accept="image/*" required>
            </div>
            <div class="form-group">
                <label>Mặt sau CCCD:</label>
                <input type="file" name="backImg" accept="image/*" required>
            </div>
            <div class="form-group">
                <label>Ảnh chân dung (Selfie):</label>
                <input type="file" name="faceImg" accept="image/*" required>
            </div>
            <button type="submit" class="btn-upload">Gửi hồ sơ đăng ký</button>
        </form>
    </div>
</body>
</html>
