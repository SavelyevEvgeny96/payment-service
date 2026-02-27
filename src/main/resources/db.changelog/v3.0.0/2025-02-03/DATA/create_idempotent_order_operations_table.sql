CREATE TABLE idempotent_orders (
    id UUID PRIMARY KEY
);

CREATE TABLE idempotent_order_operations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotent_order_id UUID REFERENCES idempotent_orders(id) ON DELETE CASCADE,
    state VARCHAR(255) NOT NULL,
    operation_type VARCHAR(255) NOT NULL,
    payment_type VARCHAR(255) NOT NULL,
    premium_amount DECIMAL(10, 2) NOT NULL,
    bank VARCHAR(255),
    payment_bank_id VARCHAR(255),
    payment_bank_url VARCHAR(255),
    depersonalization BOOLEAN DEFAULT FALSE,
    operation_started TIMESTAMP,
    operation_finished TIMESTAMP,
    create_date TIMESTAMP DEFAULT now(),
    update_date TIMESTAMP DEFAULT now()
);