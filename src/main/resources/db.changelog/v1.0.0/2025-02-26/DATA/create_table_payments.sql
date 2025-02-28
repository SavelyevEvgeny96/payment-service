CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,           -- гуид записи
    state_id VARCHAR(50) ,      -- Статус
    bank_id VARCHAR(255) ,  --  Банк
    order_id VARCHAR(255) , -- Уникальный идентификатор заказа
    type_id VARCHAR(255) , -- Тип оплаты
    payment_started VARCHAR(255) NOT NULL, -- Дата и время начала оплаты
    payment_finished VARCHAR(255),      -- Дата и время завершения оплаты
    payment_page_url VARCHAR(255) NOT NULL, -- URL страницы банка
    payment_bank_id VARCHAR(255) NOT NULL,   -- Уникальный идентификатор платежа
    create_date VARCHAR(255) NOT NULL,  -- Дата создания
    update_date VARCHAR(255) NOT NULL, -- Дата обновления
    FOREIGN KEY (bank_id) REFERENCES banks(bank_id),  -- Связь с банком
    FOREIGN KEY (order_id) REFERENCES orders(order_id),  -- Связь с заказом
    FOREIGN KEY (state_id) REFERENCES payment_status(state_id), -- Связь с статусом платежа
    FOREIGN KEY (type_id) REFERENCES payment_type(type_id) -- Связь с типом платежа
);