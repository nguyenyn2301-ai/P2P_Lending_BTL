USE p2p_lending_db;

-- Đã bỏ 'IF NOT EXISTS'
ALTER TABLE notifications ADD COLUMN link_url VARCHAR(255) NULL;

CREATE TABLE IF NOT EXISTS wallet_deposits (
    deposit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    investor_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transfer_content VARCHAR(255) NOT NULL,
    cic_pdf_url VARCHAR(255) NOT NULL,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deposit_investor FOREIGN KEY (investor_id) REFERENCES investors(investor_id) ON DELETE RESTRICT
) ENGINE=InnoDB;