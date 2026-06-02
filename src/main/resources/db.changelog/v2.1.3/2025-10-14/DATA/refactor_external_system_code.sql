-- 1) (опционально) снять DEFAULT, если он задан через enum
ALTER TABLE payment_operation_history
  ALTER COLUMN action_author_id DROP DEFAULT;

-- 2) вернуть тип к строковому
ALTER TABLE payment_operation_history
  ALTER COLUMN action_author_id TYPE varchar(16)
  USING action_author_id::text;
BEGIN;

DROP CAST IF EXISTS (varchar AS external_system_code_enum);

-- Удалит тип, если нет зависимостей (колонок/дефолтов/функций и т.д.)
DROP TYPE IF EXISTS external_system_code_enum;

COMMIT;

-- Удаляем поля, которые не нужны
ALTER TABLE payment_operation_history
DROP COLUMN IF EXISTS external_system_code;