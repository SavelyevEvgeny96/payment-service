CREATE TABLE orders (
    payment_id BIGSERIAL PRIMARY KEY,       -- Автоинкрементируемый ID
    code VARCHAR(255) NOT NULL,             -- Сгенерированный код
    state_id BIGINT NOT NULL,               -- Статус
    date_delete VARCHAR(255),               -- Дата установления статуса "Заказ помечен на удаление"
    bank_id BIGINT NOT NULL,                -- Связь с банком
    operation_id VARCHAR(255) NOT NULL,     -- Идентификатор операции
    payment_end_date VARCHAR(255),          -- Дата окончания действия ссылки
    external_system_code VARCHAR(50) NOT NULL, -- Код внешней системы
    doc_type VARCHAR(255) NOT NULL,         -- Тип документа
    policy_id VARCHAR(255) NOT NULL,        -- Идентификатор полиса
    policy_number VARCHAR(255) NOT NULL,    -- Номер полиса
    contract_id VARCHAR(255) NOT NULL,      -- Идентификатор договора
    contract_number VARCHAR(255) NOT NULL,  -- Номер договора
    insurance_program VARCHAR(255) NOT NULL,-- Программа страхования
    type_insurance VARCHAR(255) NOT NULL,   -- Вид страхования
    premium_amount VARCHAR(255) NOT NULL,   -- Размер премии
    recipient_email VARCHAR(255) NOT NULL,  -- Электронная почта страхователя
    need_receipt BOOLEAN NOT NULL,          -- Признак необходимости отправки чека
    recipient_phone VARCHAR(255) NOT NULL,  -- Мобильный телефон страхователя
    recipient_user_id VARCHAR(255) NOT NULL,  -- Идентификатор личного кабинета страхователя
    policyholder VARCHAR(255) NOT NULL,     -- ФИО страхователя
    policyholder_doc VARCHAR(255) NOT NULL, -- Серия и номер паспорта
    manager_email VARCHAR(255) NOT NULL,    -- Электронная почта менеджера
    url_to_return VARCHAR(255) NOT NULL,    -- URL для перехода после успешной оплаты
    url_to_decline VARCHAR(255) NOT NULL,   -- URL для перехода после неуспешной оплаты
    custom_url VARCHAR(255) NOT NULL,       -- URL для кастомной ссылки
    payment_page_url VARCHAR(255) NOT NULL, -- URL платежной страницы банка
    hash VARCHAR(255) NOT NULL,             -- Подпись целостности запроса
    create_date VARCHAR(255) NOT NULL,      -- Дата создания
    update_date VARCHAR(255) NOT NULL,      -- Дата обновления
    FOREIGN KEY (state_id) REFERENCES order_status(id),  -- Связь со статусом заказа
    FOREIGN KEY (bank_id) REFERENCES banks(id),         -- Связь с банком
    FOREIGN KEY (external_system_code) REFERENCES client_systems(external_system_code) -- Связь с системой клиента
);