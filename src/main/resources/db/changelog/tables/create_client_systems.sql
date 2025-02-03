CREATE TABLE client_systems (
    id BIGSERIAL PRIMARY KEY,           -- Автоинкрементируемый ID
    external_system_code VARCHAR(50) NOT NULL, -- Код системы
    external_system_name VARCHAR(255) NOT NULL -- Наименование системы
);
INSERT INTO client_systems(external_system_code,external_system_name)VALUE("LK","Личный кабинет")
INSERT INTO client_systems(external_system_code,external_system_name)VALUE("ADI","Адакта")

