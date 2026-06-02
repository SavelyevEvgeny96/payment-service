-- === Редактирование ТАБЛИЦЫ orders ===

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS subscription_id VARCHAR(255);       -- Идентификатор подписки

