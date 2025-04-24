-- Drop existing tables if they exist (H2 specific syntax)
DROP TABLE IF EXISTS fee_transaction;

-- Create the fee_transaction table (H2 specific syntax)
CREATE TABLE IF NOT EXISTS fee_transaction (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               student_id VARCHAR(255) NOT NULL,
    reference_number VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    transaction_date_time TIMESTAMP NOT NULL,
    card_number VARCHAR(255) NOT NULL,
    card_type VARCHAR(50)
    );

-- Insert sample fee transactions
INSERT INTO fee_transaction
(student_id, reference_number, amount, transaction_date_time, card_number, card_type)
VALUES
    ('STD001', 'REF-2024001', 1000.00, CURRENT_TIMESTAMP(), '****-****-****-1234', 'VISA'),
    ('STD002', 'REF-2024002', 500.00, DATEADD('HOUR', -2, CURRENT_TIMESTAMP()), '****-****-****-5678', 'MASTERCARD'),
    ('STD003', 'REF-2024003', 750.00, DATEADD('DAY', -1, CURRENT_TIMESTAMP()), '****-****-****-9012', 'VISA'),
    ('STD001', 'REF-2024004', 300.00, DATEADD('DAY', -2, CURRENT_TIMESTAMP()), '****-****-****-1234', 'VISA'),
    ('STD004', 'REF-2024005', 2500.00, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), '****-****-****-3456', 'MASTERCARD'),
    ('STD002', 'REF-2024006', 450.00, DATEADD('DAY', -3, CURRENT_TIMESTAMP()), '****-****-****-5678', 'MASTERCARD'),
    ('STD005', 'REF-2024007', 1200.00, CURRENT_TIMESTAMP(), '****-****-****-7890', 'VISA'),
    ('STD003', 'REF-2024008', 800.00, DATEADD('HOUR', -1, CURRENT_TIMESTAMP()), '****-****-****-9012', 'VISA'),
    ('STD004', 'REF-2024009', 1500.00, DATEADD('MINUTE', -30, CURRENT_TIMESTAMP()), '****-****-****-3456', 'MASTERCARD'),
    ('STD005', 'REF-2024010', 950.00, CURRENT_TIMESTAMP(), '****-****-****-7890', 'VISA');

-- Create indexes for better performance (H2 specific syntax)
CREATE INDEX IF NOT EXISTS idx_student_id ON fee_transaction(student_id);
CREATE INDEX IF NOT EXISTS idx_reference_number ON fee_transaction(reference_number);
CREATE INDEX IF NOT EXISTS idx_transaction_date ON fee_transaction(transaction_date_time);