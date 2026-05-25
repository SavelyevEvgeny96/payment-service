INSERT INTO rules_payment_type (id, bank, payment_type, operation_type, availability)
VALUES
    (gen_random_uuid(), 'ALL', 'CARD', 'PAY', true),
    (gen_random_uuid(), 'ALL', 'SBP', 'PAY', true),
    (gen_random_uuid(), 'ALL', 'CARD', 'REGISTRATION', true),
    (gen_random_uuid(), 'ALL', 'SBP', 'REGISTRATION', false),
    (gen_random_uuid(), 'GPB', 'CARD', 'RECURRENT', true),
    (gen_random_uuid(), 'AKB_RUS', 'CARD', 'RECURRENT', false),
    (gen_random_uuid(), 'GPB', 'SBP', 'RECURRENT', false),
    (gen_random_uuid(), 'AKB_RUS', 'SBP', 'RECURRENT', false),
    (gen_random_uuid(), 'GPB', 'CARD', 'REVERSAL', true),
    (gen_random_uuid(), 'AKB_RUS', 'CARD', 'REVERSAL', false),
    (gen_random_uuid(), 'GPB', 'SBP', 'REVERSAL', false),
    (gen_random_uuid(), 'AKB_RUS', 'SBP', 'REVERSAL', false)
ON CONFLICT (bank, payment_type, operation_type) DO UPDATE
SET availability = EXCLUDED.availability,
    update_date = now();
