-- === Редактирование ТАБЛИЦ ===
ALTER TABLE payments ADD COLUMN key_card VARCHAR(255); -- Ключ карты для рекурентов
ALTER TABLE orders ADD COLUMN order_id_recurrent UUID; -- UUID order_id ордера созданного через сервис заказов