
CREATE TABLE payment_status (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    state_id VARCHAR(50) NOT NULL,    -- Код статуса
    state_name VARCHAR(255) NOT NULL  -- Наименование статуса
);