
DELETE FROM payment_status WHERE state_id = 'CALLBACK_AKB';

UPDATE payment_status
SET state_name = 'Получение CALLBACK от банка'
WHERE state_id = 'CALLBACK';