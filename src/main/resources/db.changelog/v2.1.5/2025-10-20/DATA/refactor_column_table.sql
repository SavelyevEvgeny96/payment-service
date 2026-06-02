-- === Редактирование ТАБЛИЦЫ orders ===

ALTER TABLE orders
    DROP COLUMN recurrent,
    DROP COLUMN recipient_gd_id;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS unified_id VARCHAR(255);