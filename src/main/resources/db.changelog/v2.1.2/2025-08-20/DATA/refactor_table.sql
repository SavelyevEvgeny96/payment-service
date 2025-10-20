DELETE FROM config_data
WHERE param_name IN ('GpbBankPortalId', 'GPBMerchantId', 'codeLength',
'GpbBankURL','periodPay','backURLS','backURLF');

ALTER TABLE sub_orders
ADD COLUMN  create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

INSERT INTO action_type(action_name)
VALUES ('Отправка запроса для регистрации заказа в АКБ Россия');