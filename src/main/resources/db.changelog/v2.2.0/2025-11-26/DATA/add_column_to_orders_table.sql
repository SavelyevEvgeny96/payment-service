-- === Редактирование ТАБЛИЦЫ orders ===

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS skip_sending_queue BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS skip_sending_receipt BOOLEAN DEFAULT TRUE;