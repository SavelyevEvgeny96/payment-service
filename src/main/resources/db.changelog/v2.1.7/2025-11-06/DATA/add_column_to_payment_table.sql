-- === Редактирование ТАБЛИЦЫ payments ===
ALTER TABLE payments ADD COLUMN depersonalization BOOLEAN DEFAULT FALSE; -- Признак необходимости деперсонализации пользовательских данных