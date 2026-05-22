USE p2p_lending_db;

-- =========================================================================
-- KHỞI TẠO: Xóa sạch dữ liệu cũ để tránh trùng lặp hoặc lỗi khóa ngoại khi chạy lại
-- =========================================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notifications;
TRUNCATE TABLE documents;
TRUNCATE TABLE transactions;
TRUNCATE TABLE investments;
TRUNCATE TABLE loans;
TRUNCATE TABLE loan_applications;
TRUNCATE TABLE bank_accounts;
TRUNCATE TABLE investors;
TRUNCATE TABLE borrowers;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================================
-- 1. CHÈN TÀI KHOẢN USERS (Đáp ứng TC 1, 2, 5, 6, 23, 32, 39)
-- =========================================================================
INSERT INTO users (user_id, email, password, role, status) VALUES 
(1, 'admin@gmail.com', '1234', 'admin', 'active'),          -- Admin duy nhất theo spec
(99, 'admin@test.com', '123', 'admin', 'active'),             -- [TC 1, 2] Tài khoản Admin test cũ
(2, 'nguyenvanvay@gmail.com', '123', 'borrower', 'active'),   -- [TC 5] Tài khoản người vay đã tồn tại
(3, 'tranthison@gmail.com', '123', 'borrower', 'active'),     -- Người vay 2 (Dùng test tạo thêm đơn trùng)
(4, 'lethidautu@gmail.com', '123', 'investor', 'active'),     -- Nhà đầu tư 1 (Ví nhiều tiền)
(5, 'hoangp2p@gmail.com', '123', 'investor', 'active'),       -- Nhà đầu tư 2 (Ví ít tiền)
(6, 'user_banned@gmail.com', '123', 'borrower', 'suspended'); -- Tài khoản bị khóa để test đăng nhập lỗi

-- =========================================================================
-- 2. CHÈN HỒ SƠ CHI TIẾT (BORROWERS & INVESTORS) (Đáp ứng TC 7, 11, 12, 21, 22)
-- =========================================================================
-- Người vay 1: Hồ sơ chuẩn. Người vay 2: Đang để trống thông tin CCCD hoặc điểm thấp
INSERT INTO borrowers (borrower_id, first_name, last_name, id_card_number, verification_status, monthly_income, wallet_balance, credit_score, risk_level) VALUES
(2, 'Vay', 'Nguyễn Văn', '001096001234', 'verified', 25000000.00, 5000000.00, 750, 'Low'),
(3, 'Sơn', 'Trần Thị', '001096005678', 'pending', 12000000.00, 0.00, NULL, 'Medium');

-- Nhà đầu tư 1 có 50tr ví, 10tr đóng băng. Nhà đầu tư 2 chỉ có 1tr ví (để test lỗi vượt số dư)
INSERT INTO investors (investor_id, first_name, last_name, wallet_balance, frozen_balance, risk_appetite, verification_status) VALUES
(4, 'Đầu Tư', 'Lê Thị', '50000000.00', '10000000.00', 'Moderate', 'verified'), -- [TC 12] Đủ số dư đầu tư
(5, 'Hoàng', 'Phan', '1000000.00', '0.00', 'Aggressive', 'verified');          -- [TC 11] Test ví hụt để báo lỗi số dư

-- =========================================================================
-- 3. CHÈN TÀI KHOẢN NGÂN HÀNG LIÊN KẾT
-- =========================================================================
INSERT INTO bank_accounts (user_id, bank_name, account_number, account_holder) VALUES 
(2, 'Vietcombank', '1023456789', 'NGUYEN VAN VAY'),
(4, 'Techcombank', '1903456789012', 'LE THI DAU TU');

-- =========================================================================
-- 4. CHÈN ĐƠN VAY & GÓI VAY TRÊN SÀN (Đáp ứng TC 8, 9, 13, 14, 15, 23, 25, 26, 27, 31, 33)
-- =========================================================================

-- Kịch bản A: Đơn vay 1 đang hiển thị gọi vốn công khai trên sàn (Đã gom 40%) - [TC 9, 14]
INSERT INTO loan_applications (application_id, borrower_id, amount_requested, term_months, interest_rate, status, cic_issued_date, cic_pdf_url, target_bank_name, target_account_number) VALUES
(1, 2, 20000000.00, 3, 12.00, 'approved', '2026-05-01', 'http://storage.p2p.vn/cic/cic_01.pdf', 'Vietcombank', '1023456789');

INSERT INTO loans (loan_id, application_id, loan_code, total_amount, current_funded, interest_rate, service_fee, actual_disbursed, status) VALUES
(1, 1, NULL, 20000000.00, 8000000.00, 12.00, 0.00, 0.00, 'funding');

-- Kịch bản B: Đơn vay 2 mới tạo, trạng thái 'pending' để Admin vào bấm 'Duyệt' - [TC 9, 33]
INSERT INTO loan_applications (application_id, borrower_id, amount_requested, term_months, interest_rate, status, cic_issued_date, cic_pdf_url, target_bank_name, target_account_number) VALUES
(2, 3, 15000000.00, 6, 10.50, 'pending', '2026-05-10', 'http://storage.p2p.vn/cic/cic_02.pdf', 'MBBank', '0888888888');

-- Kịch bản C: Đơn vay 3 của Người vay 2 đã có trạng thái 'pending' để chặn họ tạo thêm đơn mới - [TC 23]
INSERT INTO loan_applications (application_id, borrower_id, amount_requested, term_months, interest_rate, status, cic_issued_date, cic_pdf_url, target_bank_name, target_account_number) VALUES
(3, 3, 30000000.00, 12, 11.00, 'pending', '2026-05-11', 'http://storage.p2p.vn/cic/cic_03.pdf', 'MBBank', '0888888888');

-- Kịch bản D: Gói vay siêu lớn (2 Tỷ) để kiểm tra UI xem giao diện Report có bị tràn khung số không - [TC 31]
INSERT INTO loan_applications (application_id, borrower_id, amount_requested, term_months, interest_rate, status, cic_issued_date, cic_pdf_url, target_bank_name, target_account_number) VALUES
(4, 2, 2000000000.00, 24, 13.00, 'approved', '2026-05-12', 'http://storage.p2p.vn/cic/cic_04.pdf', 'Vietcombank', '1023456789');

INSERT INTO loans (loan_id, application_id, loan_code, total_amount, current_funded, interest_rate, service_fee, actual_disbursed, status) VALUES
(2, 4, 'GV2026SIEUCAP999', 2000000000.00, 0.00, 13.00, 200000000.00, 1800000000.00, 'funding');

-- Kịch bản E: Gói vay giả lập đã gọi vốn thành công (Trạng thái 'process') phục vụ test trả nợ - [TC 16, 17, 22]
INSERT INTO loan_applications (application_id, borrower_id, amount_requested, term_months, interest_rate, status, cic_issued_date, cic_pdf_url, target_bank_name, target_account_number) VALUES
(5, 2, 7000000.00, 12, 12.00, 'approved', '2026-04-01', 'http://storage.p2p.vn/cic/cic_05.pdf', 'Vietcombank', '1023456789');

INSERT INTO loans (loan_id, application_id, loan_code, total_amount, current_funded, interest_rate, service_fee, actual_disbursed, status, due_date) VALUES
(3, 5, 'GV2026QUAHAN112', 7000000.00, 7000000.00, 12.00, 700000.00, 6300000.00, 'overdue', '2026-05-15'); -- Thiết lập quá hạn ngày 15/5/2026

-- =========================================================================
-- 5. CHÈN NHẬT KÝ ĐẦU TƯ (INVESTMENTS)
-- =========================================================================
-- Nhà đầu tư 1 đóng góp 8 triệu vào gói 1 (Đang gọi vốn)
INSERT INTO investments (loan_id, investor_id, amount_invested, status, source_bank_name, source_account_number) VALUES
(1, 4, 8000000.00, 'pending', 'Techcombank', '1903456789012');

-- Nhà đầu tư 1 bao trọn gói 7 triệu của gói 3 (Gói hiện đang quá hạn thanh toán)
INSERT INTO investments (loan_id, investor_id, amount_invested, status, source_bank_name, source_account_number, receipt_url) VALUES
(3, 4, 7000000.00, 'completed', 'Techcombank', '1903456789012', 'http://storage.p2p.vn/contracts/hd_p2p_test.pdf');

-- =========================================================================
-- 6. NHẬT KÝ BIẾN ĐỘNG DÒNG TIỀN TRÊN HỆ THỐNG (Đáp ứng thống kê TC 21, 22)
-- =========================================================================
INSERT INTO transactions (user_id, amount, transaction_type, status) VALUES
(4, 10000000.00, 'deposit', 'completed'), -- NĐT 1 nạp tiền
(2, 5000000.00, 'deposit', 'completed'),  -- Người vay 1 nạp tiền vào ví sẵn
(2, 6300000.00, 'disbursement', 'completed'); -- Người vay 1 nhận giải ngân từ gói 3 cũ

-- =========================================================================
-- 7. CHÈN HỆ THỐNG THÔNG BÁO & CHUÔNG BÁO (Đáp ứng TC 18, 19, 40)
-- =========================================================================
INSERT INTO notifications (user_id, title, message, is_read) VALUES
(2, 'Hồ sơ tài khoản được duyệt', 'Chúc mừng bạn, tài khoản đã được xác minh thành công.', TRUE),
(2, 'Cảnh báo quá hạn đóng tiền!', 'Gói vay GV2026QUAHAN112 của bạn đã vượt quá hạn ngày 15/05. Vui lòng thanh toán!', FALSE),
(4, 'Đầu tư thành công', 'Khoản đầu tư 8,000,000 VNĐ vào đơn vay số 1 đã được ghi nhận.', FALSE);