CREATE TABLE payment_status (
    id BIGSERIAL PRIMARY KEY,            -- Автоинкрементируемый ID
    state_id VARCHAR(50) ,              -- Код статуса
    state_name VARCHAR(255),  -- Наименование статуса
    CONSTRAINT unique_payment_state_id UNIQUE (state_id)
);
INSERT INTO payment_status(state_id,state_name)VALUES('NEW','Создана запись о платеже');
INSERT INTO payment_status(state_id,state_name)VALUES('REG','Платеж зарегистрирован');
INSERT INTO payment_status(state_id,state_name)VALUES('WAIT','Платеж в обработке');
INSERT INTO payment_status(state_id,state_name)VALUES('SUCCESS','Успешная оплата');
INSERT INTO payment_status(state_id,state_name)VALUES('FAIL','Неуспешная оплата');
INSERT INTO payment_status(state_id,state_name)VALUES('REFUND ','Оплата завершена успешно, а далее был выполнен возврат средств или отмена');
INSERT INTO payment_status(state_id,state_name)VALUES('DECLINED ','Клиент отказался от оплаты');
