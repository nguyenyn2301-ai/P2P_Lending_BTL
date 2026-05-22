-- Chạy file này nếu DB đã tạo từ sql.sql cũ (bổ sung cột mới)
USE p2p_lending_db;
-- Bỏ qua lỗi nếu cột đã tồn tại (chạy từng lệnh một lần)
ALTER TABLE loans ADD COLUMN paid_periods INT DEFAULT 0;
ALTER TABLE loans ADD COLUMN funding_deadline DATE NULL;

-- Tài khoản Admin theo spec (nếu chưa có)
INSERT INTO users (email, password, role, status)
SELECT 'admin@gmail.com', '1234', 'admin', 'active'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@gmail.com');
