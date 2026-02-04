CREATE TABLE idempotent_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL
);

CREATE TABLE idempotent_order_operations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotent_order_id UUID REFERENCES idempotent_orders(id) ON DELETE CASCADE,
    bank VARCHAR(255),
    payment_bank_id VARCHAR(255),
    operation_type VARCHAR(255) NOT NULL,
    payment_type VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    payment_bank_url VARCHAR(255),
    depersonalization BOOLEAN DEFAULT FALSE,
    operation_started TIMESTAMP DEFAULT now(),
    operation_finished TIMESTAMP DEFAULT now()
);