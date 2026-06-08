INSERT INTO rules_payment_type (id, bank, payment_type, operation_type, availability)
VALUES
    (gen_random_uuid(), 'GPB', 'CARD', 'PAY', true),
    (gen_random_uuid(), 'GPB', 'SBP', 'PAY', true),
    (gen_random_uuid(), 'ABR', 'CARD', 'PAY', true),
    (gen_random_uuid(), 'ABR', 'SBP', 'PAY', true)
ON CONFLICT (bank, payment_type, operation_type) DO UPDATE
SET availability = EXCLUDED.availability,
    update_date = now();
