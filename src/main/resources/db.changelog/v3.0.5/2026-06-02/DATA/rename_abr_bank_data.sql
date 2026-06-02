DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_enum e
        JOIN pg_type t ON t.oid = e.enumtypid
        WHERE t.typname = 'bank_enum' AND e.enumlabel = 'AKB_RUS'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_enum e
        JOIN pg_type t ON t.oid = e.enumtypid
        WHERE t.typname = 'bank_enum' AND e.enumlabel = 'ABR'
    ) THEN
        ALTER TYPE bank_enum RENAME VALUE 'AKB_RUS' TO 'ABR';
    END IF;
END $$;

UPDATE banks
SET bank_id = 'ABR',
    bank_name = 'АБР Россия'
WHERE bank_id IN ('abr_rus', 'abr', 'AKB_RUS');

UPDATE orders
SET bank = 'ABR'
WHERE bank::text = 'AKB_RUS';

UPDATE payments
SET bank = 'ABR'
WHERE bank::text = 'AKB_RUS';

UPDATE callback_payments
SET bank = 'ABR'
WHERE bank::text = 'AKB_RUS';

UPDATE waiting_payments
SET bank = 'ABR'
WHERE bank::text = 'AKB_RUS';

UPDATE rules_payment_type
SET bank = 'ABR'
WHERE bank = 'AKB_RUS';
