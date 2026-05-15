CREATE TABLE prioritization_rules_banks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_priority VARCHAR(16) NOT NULL,
    bank_priority_check BOOLEAN NOT NULL DEFAULT FALSE,
    bank_reserve VARCHAR(16) NOT NULL,
    part_bank_priority INTEGER NOT NULL,
    available_gpb_check BOOLEAN NOT NULL DEFAULT TRUE,
    available_abr_check BOOLEAN NOT NULL DEFAULT TRUE,
    create_date TIMESTAMP DEFAULT now(),
    update_date TIMESTAMP DEFAULT now(),
    CONSTRAINT chk_prioritization_rules_banks_bank_priority CHECK (bank_priority IN ('GPB', 'ABR')),
    CONSTRAINT chk_prioritization_rules_banks_bank_reserve CHECK (bank_reserve IN ('GPB', 'ABR')),
    CONSTRAINT chk_prioritization_rules_banks_part_bank_priority CHECK (part_bank_priority >= 0 AND part_bank_priority <= 100)
);

CREATE TABLE rules_banks_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    insurance_kind VARCHAR(255) NOT NULL,
    program VARCHAR(255) NOT NULL,
    bank VARCHAR(16) NOT NULL,
    payment_type VARCHAR(16) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    create_date TIMESTAMP DEFAULT now(),
    update_date TIMESTAMP DEFAULT now(),
    CONSTRAINT chk_rules_banks_products_bank CHECK (bank IN ('GPB', 'ABR')),
    CONSTRAINT chk_rules_banks_products_payment_type CHECK (payment_type IN ('CARD', 'SBP'))
);

CREATE TABLE history_settings_changes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action_type VARCHAR(16) NOT NULL,
    action TEXT NOT NULL,
    action_author VARCHAR(255) NOT NULL,
    action_date TIMESTAMP DEFAULT now(),
    CONSTRAINT chk_history_settings_changes_action_type CHECK (action_type IN ('ADD', 'UPDATE', 'DELETE'))
);

CREATE TABLE client_access_admin (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    create_date TIMESTAMP DEFAULT now(),
    update_date TIMESTAMP DEFAULT now()
);

INSERT INTO client_access_admin (client_id, description)
VALUES ('www-sogaz-client', 'Сайт 2.0'),
       ('osago-backend', 'ОСАГО бэк');
