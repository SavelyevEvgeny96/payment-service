ALTER TABLE orders
DROP COLUMN need_receipt,
DROP COLUMN recipient_phone,
DROP COLUMN recipient_user_id,
DROP COLUMN policyholder,
DROP COLUMN policyholder_doc,
DROP COLUMN custom_url;

ALTER TABLE sub_orders
DROP COLUMN manager_email,
DROP COLUMN hash;