CREATE TABLE payment_operation_history (
    id BIGSERIAL PRIMARY KEY,           -- Автоинкрементируемый ID
    action_id BIGINT,       -- Действие
    action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Дата выполнения операции
    action_author_id BIGINT, -- Исполнитель
    order_id VARCHAR(255),         -- Идентификатор заказа
    FOREIGN KEY (order_id) REFERENCES orders(order_id), -- Связь с заказом
    FOREIGN KEY (action_id) REFERENCES action_type(id),
    FOREIGN KEY (action_author_id) REFERENCES client_systems(id)

);