CREATE TABLE payment_operation_history (
    id BIGSERIAL PRIMARY KEY,           -- Автоинкрементируемый ID
    action VARCHAR(255) NOT NULL,       -- Действие
    action_date VARCHAR(255) NOT NULL,  -- Дата выполнения операции
    action_author VARCHAR(255) NOT NULL, -- Исполнитель
    order_id VARCHAR(255),         -- Идентификатор заказа
    FOREIGN KEY (order_id) REFERENCES orders(order_id), -- Связь с заказом
    FOREIGN KEY (action) REFERENCES action_type(id), -- Связь с заказом
    FOREIGN KEY (action_author) REFERENCES client_systems(id)

);