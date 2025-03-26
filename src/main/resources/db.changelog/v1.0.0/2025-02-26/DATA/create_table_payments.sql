CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,           -- гуид записи
    state_id VARCHAR(50) ,      -- Статус
    bank_id VARCHAR(255) ,  --  Банк
    order_id VARCHAR(255) , -- Уникальный идентификатор заказа
    type_id VARCHAR(255) , -- Тип оплаты
    payment_started VARCHAR(255) , -- Дата и время начала оплаты
    payment_finished VARCHAR(255),      -- Дата и время завершения оплаты
    payment_page_url VARCHAR(255) , -- URL страницы банка
    payment_bank_id VARCHAR(255) ,   -- Уникальный идентификатор платежа
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Дата создания, автоматически заполняется
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Дата обновления, автоматически заполняется
    FOREIGN KEY (bank_id) REFERENCES banks(bank_id),  -- Связь с банком
    FOREIGN KEY (order_id) REFERENCES orders(order_id),  -- Связь с заказом
    FOREIGN KEY (state_id) REFERENCES payment_status(state_id), -- Связь с статусом платежа
    FOREIGN KEY (type_id) REFERENCES payment_type(type_id) -- Связь с типом платежа
);