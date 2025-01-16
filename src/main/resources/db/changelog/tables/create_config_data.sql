CREATE TABLE config_data (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    param_name VARCHAR(255) NOT NULL, -- Наименование параметра
    param_value VARCHAR(255) NOT NULL -- Значение параметра
);