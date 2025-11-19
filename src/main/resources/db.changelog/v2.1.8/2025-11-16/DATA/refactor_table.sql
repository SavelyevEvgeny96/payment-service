-- === Редактирование ТАБЛИЦ ===
ALTER TABLE orders ADD COLUMN order_id_recurrent UUID; -- UUID order_id ордера созданного через сервис заказов
ALTER TABLE orders ADD COLUMN IF NOT EXISTS recurrent BOOLEAN DEFAULT FALSE;