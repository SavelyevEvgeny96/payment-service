DELETE FROM config_data
WHERE param_name IN ('GpbBankPortalId', 'GPBMerchantId', 'codeLength',
'GpbBankURL','periodPay','backURLS','backURLF');

ALTER TABLE sub_orders
ADD COLUMN  create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP;