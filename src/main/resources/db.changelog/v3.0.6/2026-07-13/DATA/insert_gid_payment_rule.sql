INSERT INTO rules_payment_type (id, bank, payment_type, operation_type, active)
VALUES (gen_random_uuid(), 'GPB', 'CARD_GID', 'PAY', true)
ON CONFLICT DO NOTHING;
