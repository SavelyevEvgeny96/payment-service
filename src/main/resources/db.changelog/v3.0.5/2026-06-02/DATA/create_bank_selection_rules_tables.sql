CREATE TABLE IF NOT EXISTS prioritization_rules_banks (
    id UUID PRIMARY KEY,
    bank_priority VARCHAR(32) NOT NULL,
    bank_priority_check BOOLEAN NOT NULL,
    bank_reserve VARCHAR(32) NOT NULL,
    part_bank_priority INTEGER NOT NULL,
    available_gpb_check BOOLEAN NOT NULL,
    available_abr_check BOOLEAN NOT NULL,
    create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT chk_prioritization_rules_banks_part_bank_priority
        CHECK (part_bank_priority >= 0 AND part_bank_priority <= 100)
);

CREATE INDEX IF NOT EXISTS idx_prioritization_rules_banks_update_date
    ON prioritization_rules_banks(update_date);

CREATE TABLE IF NOT EXISTS rules_banks_products (
    id UUID PRIMARY KEY,
    insurance_kind VARCHAR(255) NOT NULL,
    program VARCHAR(255),
    bank VARCHAR(32) NOT NULL,
    payment_type VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL,
    create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_rules_banks_products_lookup
    ON rules_banks_products(insurance_kind, payment_type, active);
