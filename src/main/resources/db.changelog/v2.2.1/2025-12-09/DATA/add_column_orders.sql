-- === Редактирование ТАБЛИЦЫ orders ===

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS reg_card BOOLEAN DEFAULT FALSE;        -- Признак регистрации карты
