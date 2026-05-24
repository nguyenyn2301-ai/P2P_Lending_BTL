<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký hệ thống P2P</title>
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
        .register-container { 
            background: #fff; 
            padding: 30px; 
            border-radius: 12px; 
            box-shadow: 0 8px 24px rgba(0,0,0,0.1); 
            width: 100%; 
            max-width: 450px; 
            box-sizing: border-box; 
            margin: 20px 0; 
        }
        h2 { text-align: center; margin-bottom: 25px; color: #333; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; font-weight: 600; color: #555; }
        .form-group input, .form-group select { 
            width: 100%; 
            padding: 12px; 
            border: 1px solid #ddd; 
            border-radius: 6px; 
            box-sizing: border-box; 
            font-size: 14px; 
        }
        
        .password-wrapper { 
            position: relative; 
            display: flex; 
            align-items: center; 
        }
        .password-wrapper input {
            padding-right: 40px;
        }
        .toggle-password { 
            position: absolute; 
            right: 12px; 
            cursor: pointer; 
            color: #888; 
            font-size: 18px; 
        }
        .toggle-password:hover { color: #333; }
        
        .btn-submit { 
            width: 100%; 
            padding: 12px; 
            background-color: #007bff; 
            border: none; 
            color: white; 
            font-size: 16px; 
            font-weight: bold; 
            border-radius: 6px; 
            cursor: pointer; 
            margin-top: 10px; 
            transition: background 0.3s;
        }
        .btn-submit:hover { background-color: #0056b3; }
        .error-msg { 
            background-color: #f8d7da; 
            color: #721c24; 
            padding: 10px; 
            border-radius: 4px; 
            margin-bottom: 20px; 
            text-align: center; 
            font-size: 14px; 
            border: 1px solid #f5c6cb;
        }
        .dynamic-section { 
            display: none; 
            border-left: 3px solid #007bff; 
            padding-left: 10px; 
            margin-top: 15px; 
        }
        .section-title {
            font-size: 15px;
            color: #333;
            margin: 0 0 14px 0;
            font-weight: 600;
        }
        .login-link { text-align: center; margin-top: 20px; font-size: 14px; }
        .login-link a { color: #007bff; text-decoration: none; }
        .terms-row { display: flex; align-items: flex-start; gap: 8px; font-size: 13px; color: #555; margin-bottom: 16px; }
        .terms-row input[type="checkbox"] { width: 16px; height: 16px; margin-top: 2px; flex-shrink: 0; }
        .terms-link { text-decoration: underline; color: #007bff; cursor: pointer; }
    </style>
</head>
<body>

<div class="register-container">
    <h2>Đăng ký hệ thống P2P</h2>
    <p style="text-align:center;color:#666;font-size:14px;margin:-12px 0 20px 0;">Bước 1 / 2 — Tạo tài khoản &amp; thông tin hồ sơ</p>

    <% 
        String error = request.getParameter("error");
        if ("passwordMismatch".equals(error)) { %>
            <div class="error-msg"><i class="fa-solid fa-circle-exclamation"></i> Mật khẩu xác nhận không trùng khớp!</div>
     <% } else if ("emailExist".equals(error)) { %>
            <div class="error-msg"><i class="fa-solid fa-circle-exclamation"></i> Email này đã được đăng ký trước đó!</div>
     <% } else if ("failed".equals(error) || "dbError".equals(error)) { %>
            <div class="error-msg"><i class="fa-solid fa-circle-exclamation"></i> Đăng ký thất bại! Hệ thống bận hoặc dữ liệu trùng lặp.</div>
     <% } else if ("missingFields".equals(error)) { %>
            <div class="error-msg"><i class="fa-solid fa-circle-exclamation"></i> Vui lòng điền đầy đủ tất cả các trường bắt buộc!</div>
     <% } else if ("invalidIncome".equals(error)) { %>
            <div class="error-msg"><i class="fa-solid fa-circle-exclamation"></i> Thu nhập nhập vào phải là một số hợp lệ!</div>
     <% } else if ("terms".equals(error)) { %>
            <div class="error-msg"><i class="fa-solid fa-circle-exclamation"></i> Bạn cần đồng ý với điều khoản sử dụng để đăng ký!</div>
     <% } 
    %>
    
    <div id="jsError" class="error-msg" style="display: none;"></div>

    <form action="RegisterController" method="POST" onsubmit="return validateForm()">
        <div class="form-group">
            <label for="role">Bạn tham gia với vai trò:</label>
            <select id="role" name="role" onchange="toggleRoleFields()" required>
                <option value="" disabled selected>-- Chọn vai trò --</option>
                <option value="borrower">Người đi vay (Borrower)</option>
                <option value="investor">Nhà đầu tư (Investor)</option>
            </select>
        </div>

        <div class="form-group">
            <label for="email">Email:</label>
            <input type="email" id="email" name="email" placeholder="example@gmail.com" required>
        </div>

        <div class="form-group">
            <label for="password">Mật khẩu:</label>
            <div class="password-wrapper">
                <input type="password" id="password" name="password" placeholder="Nhập mật khẩu..." required>
                <i class="fa-solid fa-eye toggle-password" id="eyePassword" onclick="togglePasswordVisibility('password', 'eyePassword')"></i>
            </div>
        </div>

        <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu:</label>
            <div class="password-wrapper">
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Nhập lại mật khẩu..." required>
                <i class="fa-solid fa-eye toggle-password" id="eyeConfirm" onclick="togglePasswordVisibility('confirmPassword', 'eyeConfirm')"></i>
            </div>
        </div>

        <div class="form-group">
            <label for="firstName">Tên:</label>
            <input type="text" id="firstName" name="firstName" placeholder="Nhập tên..." required>
        </div>

        <div class="form-group">
            <label for="lastName">Họ:</label>
            <input type="text" id="lastName" name="lastName" placeholder="Nhập họ..." required>
        </div>

        <div id="borrowerFields" class="dynamic-section">
            <p class="section-title">Form hồ sơ đăng ký (Người đi vay)</p>
            <div class="form-group">
                <label for="idCardNumber">Số CCCD (12 số):</label>
                <input type="text" id="idCardNumber" name="idCardNumber" placeholder="Nhập 12 số CCCD">
            </div>
            <div class="form-group">
                <label for="monthlyIncome">Thu nhập (VND):</label>
                <input type="number" id="monthlyIncome" name="monthlyIncome" min="0" step="1000" placeholder="Nhập mức thu nhập...">
            </div>
        </div>

        <div id="investorFields" class="dynamic-section">
            <p class="section-title">Form hồ sơ đăng ký (Nhà đầu tư)</p>
            <div class="form-group">
                <label for="riskAppetite">Khẩu vị rủi ro:</label>
                <select id="riskAppetite" name="riskAppetite">
                    <option value="Conservative">An toàn (Conservative)</option>
                    <option value="Moderate">Vừa phải (Moderate)</option>
                    <option value="Aggressive">Mạo hiểm (Aggressive)</option>
                </select>
            </div>
        </div>

        <div class="terms-row">
            <input type="checkbox" name="agreeTerms" id="agreeTerms" value="on">
            <label for="agreeTerms">
                Tôi đồng ý với <span class="terms-link" onclick="openTerms(event)">điều khoản sử dụng</span>
            </label>
        </div>

        <button type="submit" class="btn-submit">Hoàn tất đăng ký</button>
    </form>

    <div class="login-link">
        Đã có tài khoản? <a href="login.jsp">Đăng nhập ngay</a>
    </div>
</div>

<script>
    function togglePasswordVisibility(inputId, iconId) {
        const passwordInput = document.getElementById(inputId);
        const eyeIcon = document.getElementById(iconId);
        
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

    function openTerms(e) {
        e.preventDefault();
        window.open('terms.jsp', 'terms', 'width=800,height=600,scrollbars=yes');
    }

    function toggleRoleFields() {
        var role = document.getElementById("role").value;
        var borrowerFields = document.getElementById("borrowerFields");
        var investorFields = document.getElementById("investorFields");

        var idCard = document.getElementById("idCardNumber");
        var income = document.getElementById("monthlyIncome");

        if (role === "borrower") {
            borrowerFields.style.display = "block";
            investorFields.style.display = "none";
            idCard.required = true;
            income.required = true;
        } else if (role === "investor") {
            borrowerFields.style.display = "none";
            investorFields.style.display = "block";
            idCard.required = false;
            income.required = false;
        } else {
            borrowerFields.style.display = "none";
            investorFields.style.display = "none";
        }
    }

    function validateForm() {
        var password = document.getElementById("password").value;
        var confirmPassword = document.getElementById("confirmPassword").value;
        var role = document.getElementById("role").value;
        var jsError = document.getElementById("jsError");

        jsError.style.display = "none";
        jsError.innerHTML = "";

        if (!document.getElementById("agreeTerms").checked) {
            jsError.innerHTML = "<i class='fa-solid fa-circle-exclamation'></i> Bạn cần đồng ý với điều khoản sử dụng!";
            jsError.style.display = "block";
            return false;
        }

        if (password !== confirmPassword) {
            jsError.innerHTML = "<i class='fa-solid fa-circle-exclamation'></i> Mật khẩu xác nhận không trùng khớp!";
            jsError.style.display = "block";
            return false;
        }

        if (role === "borrower") {
            var idCard = document.getElementById("idCardNumber").value.trim();
            var idCardPattern = /^\d{12}$/;
            if (!idCardPattern.test(idCard)) {
                jsError.innerHTML = "<i class='fa-solid fa-circle-exclamation'></i> Số CCCD phải nhập chính xác 12 chữ số!";
                jsError.style.display = "block";
                return false;
            }
        }

        return true;
    }
</script>
</body>
</html>
