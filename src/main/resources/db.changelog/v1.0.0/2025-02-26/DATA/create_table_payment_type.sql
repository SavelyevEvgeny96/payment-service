CREATE TABLE payment_type (
id BIGSERIAL PRIMARY KEY,               -- Автоинкрементируемый ID
type_id VARCHAR(50) UNIQUE ,                    -- ID Типа
type_name VARCHAR(255)              -- Наименование типа
);
INSERT INTO payment_type (type_id,type_name)VALUES('sbp','Оплата через СБП');
INSERT INTO payment_type (type_id,type_name)VALUES('bankCard','Банковская карта');