<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Hệ thống Cho vay Ngang hàng P2P Lending</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        body { 
            font-family: Arial, sans-serif; 
            background-color: #f8f9fa; 
            margin: 0; 
            padding: 0; 
            display: flex; 
            justify-content: center; 
            align-items: center; 
            height: 100vh; 
        }
        .container { 
            width: 500px; 
            background: white; 
            padding: 40px; 
            border-radius: 12px; 
            box-shadow: 0px 4px 20px rgba(0,0,0,0.1); 
            text-align: center; 
        }
        h1 { color: #333; margin-bottom: 10px; font-size: 28px; }
        .subtitle { color: #666; margin-bottom: 30px; font-size: 14px; }
        
        /* Định dạng chung cho các nút điều hướng */
        .btn { 
            display: flex; 
            align-items: center; 
            justify-content: center; 
            gap: 10px; 
            padding: 14px; 
            margin-bottom: 15px; 
            color: white; 
            text-decoration: none; 
            border-radius: 6px; 
            font-size: 16px; 
            font-weight: bold; 
            transition: background 0.2s; 
        }
        
        .btn-register { background-color: #007bff; }
        .btn-register:hover { background-color: #0056b3; }
        
        .btn-login { background-color: #28a745; }
        .btn-login:hover { background-color: #218838; }
        
        .admin-section { 
            margin-top: 30px; 
            padding-top: 20px; 
            border-top: 1px dashed #ccc; 
        }
        .btn-admin { 
            background-color: #343a40; 
            font-size: 14px; 
            padding: 10px; 
        }
        .btn-admin:hover { background-color: #23272b; }
    </style>
</head>
<body>

    <div class="container">
        <h1><i class="fa-solid fa-handshake-angle" style="color: #007bff;"></i> P2P LENDING</h1>
        <p class="subtitle">Hệ thống Kết nối Tài chính Ngang hàng Toàn diện</p>
        
        <a href="register.jsp" class="btn btn-register">
            <i class="fa-solid fa-user-plus"></i> Đăng Ký Tài Khoản Mới
        </a>
        
        <a href="login.jsp" class="btn btn-login">
            <i class="fa-solid fa-right-to-bracket"></i> Đăng Nhập Hệ Thống
        </a>
        
        <div class="admin-section">
            <a href="login.jsp" class="btn btn-admin">
                <i class="fa-solid fa-user-gear"></i> Cổng Quản Trị (Admin)
            </a>
        </div>
    </div>

</body>
</html>