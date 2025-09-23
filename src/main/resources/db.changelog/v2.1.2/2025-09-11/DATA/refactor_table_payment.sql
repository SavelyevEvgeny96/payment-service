DROP TABLE IF EXISTS payments cascade;

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),              -- GUID ID платежа
    state payment_status_enum,                                  -- Текущий статус
    bank bank_enum,                                             -- Банк
    order_id UUID,                                                 -- Уникальный идентификатор заказа
    type payment_type_enum,                                     -- Тип оплаты
    cheque_name VARCHAR(20),                                    -- Статус отправки чека
    payment_pass VARCHAR(255),                                  -- Пароль заказа
    qrc_id VARCHAR(255),                                        -- Идентификатор банковского QR-кода оплаты
    payment_page_url VARCHAR(255),                              -- URL страницы банка
    payment_bank_id VARCHAR(255),                               -- Уникальный идентификатор платежа в банке
    payment_started TIMESTAMP DEFAULT CURRENT_TIMESTAMP,        -- Дата и время начала оплаты
    payment_finished TIMESTAMP,                                 -- Дата и время завершения оплаты
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,            -- Дата создания, автоматически заполняется
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,            -- Дата обновления, автоматически заполняется
    FOREIGN KEY (order_id) REFERENCES orders(id)                -- Связь с заказом
);
