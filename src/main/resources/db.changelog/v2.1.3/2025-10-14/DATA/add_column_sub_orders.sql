-- === Редактирование ТАБЛИЦЫ sub_orders ===

-- Удаляем поля, которые не нужны
ALTER TABLE sub_orders
    DROP COLUMN IF EXISTS operation_id,
    DROP COLUMN IF EXISTS external_system_code;

-- снять NOT NULL у contract_number
ALTER TABLE sub_orders
  ALTER COLUMN contract_number DROP NOT NULL;

-- Добавляем новые поля
ALTER TABLE sub_orders
    ADD COLUMN IF NOT EXISTS policy_date TIMESTAMP,               -- Дата создания полиса
    ADD COLUMN IF NOT EXISTS contract_date TIMESTAMP,             -- Дата заключения договора
    ADD COLUMN IF NOT EXISTS main_contract_check BOOLEAN,    -- Основной договор страхования
    ADD COLUMN IF NOT EXISTS manager_email VARCHAR(255),          -- Email страхового менеджера
    ADD COLUMN IF NOT EXISTS channel VARCHAR(255);                -- Канал, создавший заказ