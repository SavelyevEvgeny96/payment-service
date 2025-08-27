DROP TABLE IF EXISTS action_type CASCADE;

ALTER TABLE payment_operation_history
DROP COLUMN action_id;

ALTER TABLE payment_operation_history
ADD COLUMN action VARCHAR(255);