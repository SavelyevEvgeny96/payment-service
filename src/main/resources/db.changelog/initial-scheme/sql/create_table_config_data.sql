
CREATE TABLE config_data (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    param_name VARCHAR(255) NOT NULL, -- Наименование параметра
    param_value VARCHAR(255) , -- Значение параметра
    param_description VARCHAR(255) NOT NULL -- Описание параметра
);
INSERT INTO config_data(param_name,param_description,param_value)VALUES('GpbBankURL','Адрес для взаимодействия с Газпром банком (оплата банковской картой)','https://lt.pga.gazprombank.ru/api/v4/');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('bankPriorityCheck','Фигитогл. При установки значения true все оплаты по банковской карте будут осуществляться только через банк, указанный в параметре bankPriority','false');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('bankPriority','Приоритетный банк для совершения платежей по банковской карте. Возможные значения должны соответствовать значениям из таблицы \"Банки для оплат\" параметр bankId','gpb');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('bankReserve','Запасной банк для совершения платежей по банковской карте. Возможные значения должны соответствовать значениям из таблицы "Банки для оплат" параметр bankId','gpb');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('GpbBankPortalId ','Идентификатор портала от Газпром банка','');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('GPBMerchantId','Идентификатор магазина от Газпром банка','');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('backURLS','Адрес для возврата на страницу успешной оплаты','');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('backURLF','Адрес для возврата на страницу ошибки в результате оплаты','');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('codeLenght ','Количество символов для генерации короткого кода','6');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('paymentCheckURL','Адрес сервиса генерации электронных чеков','');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('periodPay','Периодичность запуска фоновой задачи запроса статуса платежа (в минутах)','1');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('hostName','URL страницы оплаты сервиса','www.pay.sogaz.ru');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('GpbBankSBPURL','Адрес для оплаты через СБП (Газпром банк)','');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('GPBSBPMerchantId','Идентификатор магазина от Газпром банка для СБП','');
INSERT INTO config_data(param_name,param_description,param_value)VALUES('qrTtl','Срок жизни QR в минутах','15');

