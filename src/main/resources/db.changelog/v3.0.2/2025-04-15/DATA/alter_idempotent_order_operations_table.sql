ALTER TABLE idempotent_order_operations
    ADD COLUMN pay_items JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN description TEXT,
    ADD COLUMN external_error_code VARCHAR(255),
    ADD COLUMN error_text TEXT;