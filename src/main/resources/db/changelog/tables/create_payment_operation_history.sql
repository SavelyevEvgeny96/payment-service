CREATE TABLE payment_operation_history (
    id BIGSERIAL PRIMARY KEY,           -- Автоинкрементируемый ID
    action VARCHAR(255) NOT NULL,       -- Действие
    action_date VARCHAR(255) NOT NULL,  -- Дата выполнения операции
    action_author VARCHAR(255) NOT NULL, -- Исполнитель
    payment_id BIGINT NOT NULL,         -- Идентификатор заказа
    FOREIGN KEY (payment_id) REFERENCES payments(id) -- Связь с платежом
);
