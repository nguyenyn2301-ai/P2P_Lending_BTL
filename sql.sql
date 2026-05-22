CREATE DATABASE IF NOT EXISTS p2p_lending_db;
USE p2p_lending_db;

-- =========================================================================
-- KHỞI TẠO: Xóa bảng cũ theo thứ tự ngược lại để tránh xung đột Foreign Key
-- =========================================================================
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS documents;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS investments;
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS loan_applications;
DROP TABLE IF EXISTS bank_accounts;
DROP TABLE IF EXISTS investors;
DROP TABLE IF EXISTS borrowers;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================================
-- KHỐI 1: TÀI KHOẢN NỀN TẢNG & THÔNG TIN ĐỊNH DANH (GIỮ NGUYÊN TÊN CỘT GỐC)
-- =========================================================================

-- 1. Table: users
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'investor', 'borrower') NOT NULL,
    status ENUM('active', 'suspended', 'inactive') DEFAULT 'active', -- Phục vụ khóa/mở tài khoản mềm
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Table: borrowers
CREATE TABLE borrowers (
    borrower_id BIGINT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    id_card_number VARCHAR(20) NOT NULL UNIQUE,
    verification_status ENUM('pending', 'verified', 'rejected') DEFAULT 'pending',
    monthly_income DECIMAL(15,2) NOT NULL,
    wallet_balance DECIMAL(15,2) DEFAULT 0.00, -- Ví tiền xử lý dòng tiền trung gian
    credit_score INT NULL,
    risk_level ENUM('Low', 'Medium', 'High', 'Very High') DEFAULT 'Medium',
    CONSTRAINT fk_borrower_user FOREIGN KEY (borrower_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Table: investors
CREATE TABLE investors (
    investor_id BIGINT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    wallet_balance DECIMAL(15,2) DEFAULT 0.00,
    frozen_balance DECIMAL(15,2) DEFAULT 0.00, -- Đóng băng tiền khi đang trong phòng gọi vốn
    risk_appetite ENUM('Conservative', 'Moderate', 'Aggressive') NOT NULL,
    verification_status ENUM('pending', 'verified', 'rejected') DEFAULT 'pending',
    CONSTRAINT fk_investor_user FOREIGN KEY (investor_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Table: bank_accounts
CREATE TABLE bank_accounts (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_holder VARCHAR(100) NOT NULL,
    CONSTRAINT fk_bank_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =========================================================================
-- KHỐI 2: ĐƠN XIN VAY & SÀN GỌI VỐN ĐẦU TƯ GÓP VỐN CỘNG ĐỒNG
-- =========================================================================

-- 5. Table: loan_applications
CREATE TABLE loan_applications (
    application_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    borrower_id BIGINT NOT NULL,
    amount_requested DECIMAL(15,2) NOT NULL,
    term_months INT NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL, -- Đồng bộ trường lãi suất từ form đề xuất
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending', -- Không duyệt -> Admin chạy lệnh xóa đơn
    cic_issued_date DATE NOT NULL,
    cic_pdf_url VARCHAR(255) DEFAULT NULL, -- Chuyển sang mẫu linh hoạt cho phép null ban đầu
    target_bank_name VARCHAR(100) DEFAULT NULL,      
    target_account_number VARCHAR(20) DEFAULT NULL,  
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_borrower FOREIGN KEY (borrower_id) REFERENCES borrowers(borrower_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 6. Table: loans
CREATE TABLE loans (
    loan_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    loan_code VARCHAR(100) UNIQUE NULL,         -- Lưu mã định danh đặc biệt dạng GV2026KHANHYEN...
    total_amount DECIMAL(15,2) NOT NULL,
    current_funded DECIMAL(15,2) DEFAULT 0.00,
    interest_rate DECIMAL(5,2) NOT NULL,
    service_fee DECIMAL(15,2) DEFAULT 0.00,     -- Phí dịch vụ sàn 10% cấu véo lại
    actual_disbursed DECIMAL(15,2) DEFAULT 0.00, -- Số tiền thực tế chuyển khoản (Gói vốn - 10%)
    status ENUM('funding', 'process', 'failed', 'overdue', 'completed') DEFAULT 'funding', -- Đổi 'active' -> 'process' theo đúng flow bạn yêu cầu
    borrower_confirmed BOOLEAN DEFAULT FALSE,    -- Bên vay bấm nút hoàn thành
    investor_confirmed BOOLEAN DEFAULT FALSE,    -- Bên cho vay bấm nút hoàn thành
    due_date DATE NULL,                          -- Hạn chót kỳ trả nợ hiện tại
    paid_periods INT DEFAULT 0,                  -- Số kỳ đã thanh toán
    funding_deadline DATE NULL,                  -- Hạn gọi vốn trên sàn (30 ngày)
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- Thiết lập CASCADE để khi đơn nháp bị Admin xóa, gói nháp tự động bốc hơi theo
    CONSTRAINT fk_loan_app FOREIGN KEY (application_id) REFERENCES loan_applications(application_id) ON DELETE CASCADE,
    CONSTRAINT chk_loan_funding_limit CHECK (current_funded <= total_amount)
) ENGINE=InnoDB;

-- 7. Table: investments
CREATE TABLE investments (
    investment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    investor_id BIGINT NOT NULL,
    amount_invested DECIMAL(15,2) NOT NULL,
    status ENUM('pending', 'completed') DEFAULT 'completed',
    source_bank_name VARCHAR(100) DEFAULT NULL,      
    source_account_number VARCHAR(20) DEFAULT NULL,  
    receipt_url VARCHAR(255) NULL,                   -- Đường dẫn chứa file Hợp đồng P2P điện tử dạng form mẫu
    invested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Thiết lập CASCADE để nếu đơn bị xóa thì sạch nợ nháp, gói 'failed' thì giữ nguyên xem lịch sử
    CONSTRAINT fk_inv_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_investor FOREIGN KEY (investor_id) REFERENCES investors(investor_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =========================================================================
-- KHỐI 3: NHẬT KÝ BIẾN ĐỘNG DÒNG TIỀN, HỒ SƠ CHỨNG TỪ & THÔNG BÁO
-- =========================================================================

-- 9. Table: transactions
CREATE TABLE transactions (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_type ENUM('deposit', 'withdraw', 'disbursement', 'repayment', 'service_fee_deducted', 'refund') NOT NULL, -- Mở rộng cấu trúc giao dịch
    status ENUM('pending', 'completed', 'failed') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trans_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 10. Table: documents
CREATE TABLE documents (
    document_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    document_type ENUM('id_card_front', 'id_card_back', 'salary_slip', 'contract', 'other') NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doc_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. Table: notifications
CREATE TABLE notifications (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;