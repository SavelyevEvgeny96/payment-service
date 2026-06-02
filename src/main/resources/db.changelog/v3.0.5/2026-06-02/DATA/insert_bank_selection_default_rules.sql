INSERT INTO prioritization_rules_banks (
    id,
    bank_priority,
    bank_priority_check,
    bank_reserve,
    part_bank_priority,
    available_gpb_check,
    available_abr_check
)
SELECT
    gen_random_uuid(),
    'GPB',
    false,
    'ABR',
    100,
    true,
    false
WHERE NOT EXISTS (SELECT 1 FROM prioritization_rules_banks);
