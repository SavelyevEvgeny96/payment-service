
DELETE FROM action_type WHERE action_name = 'Получение CALLBACK от АКБ Россия';

UPDATE action_type
SET action_name = 'Получение CALLBACK от банка'
WHERE action_name = 'Получение CALLBACK от ГПБ';