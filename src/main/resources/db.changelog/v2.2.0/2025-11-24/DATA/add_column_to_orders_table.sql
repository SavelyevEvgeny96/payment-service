-- === Редактирование ТАБЛИЦЫ orders ===

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS queue_status_result_name VARCHAR(255);