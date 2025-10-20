drop table sub_orders cascade;

create table sub_orders (
   id UUID primary key default gen_random_uuid(), 	    -- GUID ID подзаказа
   order_id UUID,        						        -- GUID ID связанного заказа
   operation_id VARCHAR(255) ,     		                -- Идентификатор операции
   external_system_code external_system_code_enum, 	    -- Код внешней системы
   doc_type VARCHAR(255),                               -- Тип документа
   policy_id VARCHAR(255) NOT NULL,                     -- Идентификатор полиса
   policy_number VARCHAR(255) NOT NULL,                 -- Номер полиса
   contract_id VARCHAR(255),                            -- Идентификатор договора
   contract_number VARCHAR(255) NOT NULL,               -- Номер договора
   insurance_program VARCHAR(255),                      -- Программа страхования
   type_insurance VARCHAR(255),                         -- Вид страхования
   premium_amount VARCHAR(255),   			            -- Размер премии
   create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,     -- Дата создания, автоматически заполняется
   update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,     -- Дата обновления, автоматически заполняется
   hash VARCHAR(255),                                   -- Подпись целостности запроса
   FOREIGN KEY (order_id) REFERENCES orders(id)         -- Связь с айди заказа
);