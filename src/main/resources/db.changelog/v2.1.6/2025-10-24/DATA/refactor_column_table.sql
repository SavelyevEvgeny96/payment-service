-- === Редактирование ТАБЛИЦЫ orders ===

DELETE FROM client_systems WHERE external_system_code = 'ADI';
INSERT INTO client_systems(external_system_code,external_system_name)VALUES('adinsure-client','Адакта');
INSERT INTO client_systems(external_system_code,external_system_name)VALUES('ordering-client','Сервис подписок');