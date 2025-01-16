

CREATE TABLE banks (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    bank_id VARCHAR(50) NOT NULL,     -- Код банка
    bank_name VARCHAR(255) NOT NULL   -- Наименование банка
);