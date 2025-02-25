CREATE TABLE sub_orders (
   id BIGSERIAL ,
   sub_order_id VARCHAR(255) PRIMARY KEY,     -- GUID ID подзаказа
   order_id VARCHAR(255),        -- GUID ID заказа
   operation_id VARCHAR(255) ,     -- Идентификатор операции
   external_system_code VARCHAR(50) , -- Код внешней системы
   doc_type VARCHAR(255),                  -- Тип документа
   policy_id VARCHAR(255) NOT NULL,        -- Идентификатор полиса
   policy_number VARCHAR(255) NOT NULL,    -- Номер полиса
   contract_id VARCHAR(255),               -- Идентификатор договора
   contract_number VARCHAR(255) NOT NULL,  -- Номер договора
   insurance_program VARCHAR(255),         -- Программа страхования
   type_insurance VARCHAR(255),            -- Вид страхования
   premium_amount VARCHAR(255),   -- Размер премии
   manager_email VARCHAR(255),             -- Электронная почта менеджера
   hash VARCHAR(255),                      -- Подпись целостности запроса
   FOREIGN KEY (external_system_code) REFERENCES client_systems(external_system_code),-- Связь с системой клиента
   FOREIGN KEY (order_id) REFERENCES orders(order_id) -- Связь с айди заказа
);