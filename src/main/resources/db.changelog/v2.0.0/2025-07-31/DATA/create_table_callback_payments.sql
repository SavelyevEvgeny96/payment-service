CREATE TABLE callback_payments (
    id BIGSERIAL PRIMARY KEY,
    bank_id VARCHAR(50) NOT NULL,
    type_id VARCHAR(50) NOT NULL,
    payment_bank_id VARCHAR(255) NOT NULL,
    qrc_id VARCHAR(255),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);