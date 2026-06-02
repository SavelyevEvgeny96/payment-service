ALTER TABLE idempotent_order_operations
    ADD COLUMN IF NOT EXISTS payment_pass VARCHAR(255);
