-- === Редактирование ТАБЛИЦЫ payments ===
ALTER TABLE payments ADD COLUMN key_card VARCHAR(255); -- Ключ карты для рекурентов

ALTER TABLE payments
DROP CONSTRAINT payments_order_id_fkey;