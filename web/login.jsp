<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập hệ thống P2P Lending</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f7f6;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .login-box {
            background: #fff;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 400px;
        }
        h2 { text-align: center; color: #333; margin-bottom: 25px; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; font-weight: 600; color: #555; }
        .form-group input {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 6px;
            box-sizing: border-box;
        }
        .password-wrapper {
            position: relative;
            display: flex;
            align-items: center;
        }
        .toggle-password {
            position: absolute;
            right: 12px;
            cursor: pointer;
            color: #888;
            font-size: 18px;
        }
        .toggle-password:hover { color: #333; }
        .btn-login {
            width: 100%;
            padding: 12px;
            background-color: #007bff;
            border: none;
            color: white;
            font-size: 16px;
            font-weight: bold;
            border-radius: 6px;
            cursor: pointer;
            transition: background 0.3s;
        }
        .btn-login:hover { background-color: #0056b3; }
        .error-msg {
            background-color: #f8d7da;
            color: #721c24;
            padding: 10px;
            border-radius: 4px;
            text-align: center;
            margin-bottom: 20px;
            font-size: 14px;
            border: 1px solid #f5c6cb;
        }
        .footer-link { text-align: center; margin-top: 20px; font-size: 14px; }
        .footer-link a { color: #007bff; text-decoration: none; }
    </style>
</head>
<body>

<div class="login-box">
    <h2>Đăng Nhập P2P</h2>

    <% if ("registered".equals(request.getParameter("success"))) { %>
        <div class="error-msg" style="background-color:#d1e7dd;color:#0f5132;border-color:#badbcc;">
            <i class="fa-solid fa-circle-check"></i> Đăng ký thành công. Vui lòng đăng nhập để nộp hồ sơ eKYC (nếu chưa hoàn tất).
        </div>
    <% } %>
    <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="error-msg">
            <i class="fa-solid fa-circle-exclamation"></i> 
            <%= request.getAttribute("errorMessage") %>
        </div>
    <% } %>

    <form action="LoginController" method="POST">
        <div class="form-group">
            <label>Email</label>
            <input type="email" name="email" placeholder="Nhập email của bạn..." 
                   value="<%= (request.getAttribute("oldEmail") != null) ? request.getAttribute("oldEmail") : "" %>" required>
        </div>

        <div class="form-group">
            <label>Mật khẩu</label>
            <div class="password-wrapper">
                <input type="password" name="password" id="loginPassword" placeholder="Nhập mật khẩu..." required>
                <i class="fa-solid fa-eye toggle-password" id="eyeIcon" onclick="togglePassword()"></i>
            </div>
        </div>

        <button type="submit" class="btn-login">Hoàn tất đăng nhập</button>
    </form>

    <div class="footer-link">
        Chưa có tài khoản? <a href="register.jsp">Đăng ký tại đây</a>
    </div>
</div>

<script>
    function togglePassword() {
        const passwordInput = document.getElementById("loginPassword");
        const eyeIcon = document.getElementById("eyeIcon");
        if (passwordInput.type === "password") {
            passwordInput.type = "text";
            eyeIcon.classList.remove("fa-eye");
            eyeIcon.classList.add("fa-eye-slash");
        } else {
            passwordInput.type = "password";
            eyeIcon.classList.remove("fa-eye-slash");
            eyeIcon.classList.add("fa-eye");
        }
    }
</script>

</body>
</html>
