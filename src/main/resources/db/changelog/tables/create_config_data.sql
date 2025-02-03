CREATE TABLE config_data (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    param_name VARCHAR(255) NOT NULL, -- Наименование параметра
    param_value VARCHAR(255) NOT NULL -- Значение параметра
);
INSERT INTO config_data(param_name,param_value)VALUE("GpbBankURL","Адрес для взаимодействия с Газпром банком (оплата банковской картой)")
INSERT INTO config_data(param_name,param_value)VALUE("bankPriorityCheck","Фигитогл. При установки значения true все оплаты по банковской карте будут осуществляться только через банк, указанный в параметре bankPriority")
INSERT INTO config_data(param_name,param_value)VALUE("bankPriority","Приоритетный банк для совершения платежей по банковской карте. Возможные значения должны соответствовать значениям из таблицы \"Банки для оплат\" параметр bankId")