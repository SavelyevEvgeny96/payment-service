-- === Редактирование ТАБЛИЦЫ orders ===

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS client_id VARCHAR(255);       -- id системы из токена

