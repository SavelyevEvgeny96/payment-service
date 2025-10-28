DROP TABLE callback_payments CASCADE;

CREATE TABLE callback_payments (
    id BIGSERIAL PRIMARY KEY,
    bank bank_enum NOT NULL,
    type payment_type_enum NOT NULL,
    payment_bank_id VARCHAR(255) NOT NULL,
    payment_pass VARCHAR(255),
    qrc_id VARCHAR(255),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);