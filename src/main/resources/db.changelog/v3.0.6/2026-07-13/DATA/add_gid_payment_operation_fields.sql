ALTER TABLE idempotent_order_operations
    ADD COLUMN IF NOT EXISTS portal_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS gid_id VARCHAR(255);
