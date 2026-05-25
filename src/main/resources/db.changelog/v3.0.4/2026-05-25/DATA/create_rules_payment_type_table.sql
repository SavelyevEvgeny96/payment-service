CREATE TABLE IF NOT EXISTS rules_payment_type (
    id UUID PRIMARY KEY,
    bank VARCHAR(32) NOT NULL,
    payment_type VARCHAR(32) NOT NULL,
    operation_type VARCHAR(32) NOT NULL,
    availability BOOLEAN NOT NULL,
    create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_rules_payment_type_bank_payment_operation
    ON rules_payment_type(bank, payment_type, operation_type);

CREATE INDEX IF NOT EXISTS idx_rules_payment_type_lookup
    ON rules_payment_type(operation_type, payment_type, bank);
