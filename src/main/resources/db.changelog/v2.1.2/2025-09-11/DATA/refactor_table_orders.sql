DROP TABLE IF EXISTS orders CASCADE;

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),      -- GUID ID заказа
    status order_status_enum,               	        -- Статус
    bank bank_enum,                                  -- Банк
    premium_amount VARCHAR(255),                        -- Размер платежа
    recipient_email VARCHAR(255) NOT NULL,              -- Электронная почта страхователя
    url_to_return VARCHAR(255),                         -- URL для перехода после успешной оплаты
    url_to_decline VARCHAR(255),                        -- URL для перехода после неуспешной оплаты
    payment_end_date TIMESTAMP,          	            -- Дата окончания действия ссылки
    date_delete TIMESTAMP,               	            -- Дата установления статуса "Заказ помечен на удаление"
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- Дата создания, автоматически заполняется
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP     -- Дата обновления, автоматически заполняется
);

