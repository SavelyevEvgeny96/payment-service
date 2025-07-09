CREATE TABLE banks (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    bank_id VARCHAR(255) UNIQUE,     -- Код банка
    bank_name VARCHAR(255) NOT NULL   -- Наименование банка
);
INSERT INTO banks(bank_id,bank_name)VALUES('gpb','Газпромбанк');