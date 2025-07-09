
CREATE TABLE orders (
    id BIGSERIAL ,
    order_id VARCHAR(255) PRIMARY KEY,         -- GUID ID заказа
    code VARCHAR(255) NOT NULL,             -- Сгенерированный код
    state_id VARCHAR(255) ,               -- Статус
    date_delete VARCHAR(255),               -- Дата установления статуса "Заказ помечен на удаление"
    bank_id VARCHAR(255) ,                       -- Связь с банком
    payment_end_date VARCHAR(255),          -- Дата окончания действия ссылки
    premium_amount VARCHAR(255),            -- Размер премии
    recipient_email VARCHAR(255) NOT NULL,  -- Электронная почта страхователя
    need_receipt BOOLEAN,                   -- Признак необходимости отправки чека
    recipient_phone VARCHAR(255) NOT NULL,  -- Мобильный телефон страхователя
    recipient_user_id VARCHAR(255),         -- Идентификатор личного кабинета страхователя
    policyholder VARCHAR(255),              -- ФИО страхователя
    policyholder_doc VARCHAR(255),          -- Серия и номер паспорта
    url_to_return VARCHAR(255) ,            -- URL для перехода после успешной оплаты
    url_to_decline VARCHAR(255),            -- URL для перехода после неуспешной оплаты
    custom_url VARCHAR(255),                -- URL для кастомной ссылки
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Дата создания, автоматически заполняется
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Дата обновления, автоматически заполняется
   FOREIGN KEY (state_id) REFERENCES order_status(state_id),  -- Связь со статусом заказа
   FOREIGN KEY (bank_id) REFERENCES banks(bank_id)         -- Связь с банком
    );
