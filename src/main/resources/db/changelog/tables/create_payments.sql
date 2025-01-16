
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,           -- Автоинкрементируемый ID
    state_id VARCHAR(50) NOT NULL,      -- Статус
    bank_id BIGINT NOT NULL,            -- Ссылка на банк
    payment_started VARCHAR(255) NOT NULL, -- Дата и время начала оплаты
    payment_finished VARCHAR(255),      -- Дата и время завершения оплаты
    payment_page_url VARCHAR(255) NOT NULL, -- URL страницы банка
    payment_id VARCHAR(255) NOT NULL,   -- Уникальный идентификатор платежа
    create_date VARCHAR(255) NOT NULL,  -- Дата создания
    update_date VARCHAR(255) NOT NULL, -- Дата обновления
    FOREIGN KEY (bank_id) REFERENCES banks(id)  -- Связь с банком
);