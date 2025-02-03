
CREATE TABLE payment_status (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    state_id VARCHAR(50) NOT NULL,    -- Код статуса
    state_name VARCHAR(255) NOT NULL  -- Наименование статуса
);
INSERT INTO payment_status(state_id,state_name)VALUE("0","Платеж создан")
INSERT INTO payment_status(state_id,state_name)VALUE("1","Платеж в обработке")
INSERT INTO payment_status(state_id,state_name)VALUE("2","Успешная оплата")
INSERT INTO payment_status(state_id,state_name)VALUE("3","Неуспешная оплата")