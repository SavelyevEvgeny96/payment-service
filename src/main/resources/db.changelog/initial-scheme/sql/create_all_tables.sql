
CREATE TABLE action_type (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    action_name VARCHAR(255) NOT NULL  -- Наименование действия
);
INSERT INTO action_type(action_name)VALUES('Сохранение заказа');
INSERT INTO action_type(action_name)VALUES('Обновление заказа');
INSERT INTO action_type(action_name)VALUES('Пользователь перенаправлен на страницу банка');
INSERT INTO action_type(action_name)VALUES('Удаление заказа');
INSERT INTO action_type(action_name)VALUES('Ошибка при совершени платежа');
INSERT INTO action_type(action_name)VALUES('Заказ оплачен');
INSERT INTO action_type(action_name)VALUES('Старт платежа');
INSERT INTO action_type(action_name)VALUES('Получение токена доступа');



CREATE TABLE banks (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    bank_id VARCHAR(50) NOT NULL,     -- Код банка
    bank_name VARCHAR(255) NOT NULL,   -- Наименование банка
    CONSTRAINT unique_bank_id UNIQUE (bank_id)
);
INSERT INTO banks(bank_id,bank_name)VALUES('gpb','Газпромбанк');

CREATE TABLE client_systems (
    id BIGSERIAL PRIMARY KEY,            -- Автоинкрементируемый ID
    external_system_code VARCHAR(50) NOT NULL, -- Код системы
    external_system_name VARCHAR(255) NOT NULL, -- Наименование системы
    CONSTRAINT unique_external_system_code UNIQUE (external_system_code) -- Уникальность для использования в качестве внешнего ключа
);
INSERT INTO client_systems(external_system_code,external_system_name)VALUES('LK','Личный кабинет');
INSERT INTO client_systems(external_system_code,external_system_name)VALUES('ADI','Адакта');

CREATE TABLE config_data (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    param_name VARCHAR(255) NOT NULL, -- Наименование параметра
    param_value VARCHAR(255) NOT NULL -- Значение параметра
);

INSERT INTO config_data(param_name,param_value)VALUES('GpbBankURL','Адрес для взаимодействия с Газпром банком (оплата банковской картой)');
INSERT INTO config_data(param_name,param_value)VALUES('bankPriorityCheck','Фигитогл. При установки значения true все оплаты по банковской карте будут осуществляться только через банк, указанный в параметре bankPriority');
INSERT INTO config_data(param_name,param_value)VALUES('bankPriority','Приоритетный банк для совершения платежей по банковской карте. Возможные значения должны соответствовать значениям из таблицы \"Банки для оплат\" параметр bankId');



CREATE TABLE order_status (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    state_id VARCHAR(50) NOT NULL,    -- Код статуса
    state_name VARCHAR(255) NOT NULL , -- Наименование статуса
    CONSTRAINT unique_state_id UNIQUE (state_id)
);
INSERT INTO order_status(state_id,state_name)VALUES('NEW','Заказ создан');
INSERT INTO order_status(state_id,state_name)VALUES('UPDATE','Заказ актуализирован (ранее по данному полису уже была сгенерирована ссылка, в результате запроса данные по ней обновлены)');
INSERT INTO order_status(state_id,state_name)VALUES('OVERDUE','Заказ просрочен');
INSERT INTO order_status(state_id,state_name)VALUES('MARKEDDEL','Заказ помечен на удаление');
INSERT INTO order_status(state_id,state_name)VALUES('SUCCESS','Заказ оплачен');

CREATE TABLE orders (
    order_id VARCHAR(255) PRIMARY KEY,         -- GUID ID заказа
    code VARCHAR(255) NOT NULL,             -- Сгенерированный код
    state_id VARCHAR(255)  NOT NULL,               -- Статус
    date_delete VARCHAR(255),               -- Дата установления статуса "Заказ помечен на удаление"
    bank_id VARCHAR(255) NOT NULL,                       -- Связь с банком
    payment_end_date VARCHAR(255),          -- Дата окончания действия ссылки
    premium_amount VARCHAR(255) NOT NULL,   -- Размер премии
    recipient_email VARCHAR(255) NOT NULL,  -- Электронная почта страхователя
    need_receipt BOOLEAN,                   -- Признак необходимости отправки чека
    recipient_phone VARCHAR(255) NOT NULL,  -- Мобильный телефон страхователя
    recipient_user_id VARCHAR(255),         -- Идентификатор личного кабинета страхователя
    policyholder VARCHAR(255),              -- ФИО страхователя
    policyholder_doc VARCHAR(255),          -- Серия и номер паспорта
    url_to_return VARCHAR(255) ,            -- URL для перехода после успешной оплаты
    url_to_decline VARCHAR(255),            -- URL для перехода после неуспешной оплаты
    custom_url VARCHAR(255),                -- URL для кастомной ссылки
   FOREIGN KEY (state_id) REFERENCES order_status(state_id),  -- Связь со статусом заказа
   FOREIGN KEY (bank_id) REFERENCES banks(bank_id)         -- Связь с банком
    );
CREATE TABLE sub_orders (
   sub_order_id VARCHAR(255) PRIMARY KEY,     -- GUID ID подзаказа
   order_id VARCHAR(255) NOT NULL,        -- GUID ID заказа
   operation_id VARCHAR(255) NOT NULL,     -- Идентификатор операции
   external_system_code VARCHAR(50) NOT NULL, -- Код внешней системы
   doc_type VARCHAR(255),                  -- Тип документа
   policy_id VARCHAR(255) NOT NULL,        -- Идентификатор полиса
   policy_number VARCHAR(255) NOT NULL,    -- Номер полиса
   contract_id VARCHAR(255),               -- Идентификатор договора
   contract_number VARCHAR(255) NOT NULL,  -- Номер договора
   insurance_program VARCHAR(255),         -- Программа страхования
   type_insurance VARCHAR(255),            -- Вид страхования
   premium_amount VARCHAR(255) NOT NULL,   -- Размер премии
   manager_email VARCHAR(255),             -- Электронная почта менеджера
   hash VARCHAR(255),                      -- Подпись целостности запроса
   create_date VARCHAR(255) NOT NULL,      -- Дата создания
   update_date VARCHAR(255) NOT NULL,      -- Дата обновления
   FOREIGN KEY (external_system_code) REFERENCES client_systems(external_system_code),-- Связь с системой клиента
   FOREIGN KEY (order_id) REFERENCES orders(order_id) -- Связь с айди заказа
);


CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,           -- Автоинкрементируемый ID
    state_id VARCHAR(50) NOT NULL,      -- Статус
    bank_id VARCHAR(255) NOT NULL,            -- Ссылка на банк
    payment_started VARCHAR(255) NOT NULL, -- Дата и время начала оплаты
    payment_finished VARCHAR(255),      -- Дата и время завершения оплаты
    payment_page_url VARCHAR(255) NOT NULL, -- URL страницы банка
    payment_id VARCHAR(255) NOT NULL,   -- Уникальный идентификатор платежа
    create_date VARCHAR(255) NOT NULL,  -- Дата создания
    update_date VARCHAR(255) NOT NULL, -- Дата обновления
    FOREIGN KEY (bank_id) REFERENCES banks(bank_id)  -- Связь с банком
);

CREATE TABLE payment_operation_history (
    id BIGSERIAL PRIMARY KEY,           -- Автоинкрементируемый ID
    action VARCHAR(255) NOT NULL,       -- Действие
    action_date VARCHAR(255) NOT NULL,  -- Дата выполнения операции
    action_author VARCHAR(255) NOT NULL, -- Исполнитель
    payment_id BIGINT NOT NULL,         -- Идентификатор заказа
    FOREIGN KEY (payment_id) REFERENCES payments(id) -- Связь с платежом
);

CREATE TABLE payment_status (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    state_id VARCHAR(50) NOT NULL,    -- Код статуса
    state_name VARCHAR(255) NOT NULL  -- Наименование статуса
);
INSERT INTO payment_status(state_id,state_name)VALUES('0','Платеж создан');
INSERT INTO payment_status(state_id,state_name)VALUES('1','Платеж в обработке');
INSERT INTO payment_status(state_id,state_name)VALUES('2','Успешная оплата');
INSERT INTO payment_status(state_id,state_name)VALUES('3','Неуспешная оплата');

