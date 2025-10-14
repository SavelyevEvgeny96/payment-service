-- === Обновление ТАБЛИЦЫ orders ===

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS recipient_phone VARCHAR(255),        -- Мобильный телефон страхователя
    ADD COLUMN IF NOT EXISTS recipient_user_id VARCHAR(255),      -- Идентификатор личного кабинета страхователя
    ADD COLUMN IF NOT EXISTS policyholder VARCHAR(255),           -- ФИО страхователя
    ADD COLUMN IF NOT EXISTS recipient_gd_id VARCHAR(255),        -- Идентификатор золотой карточки
    ADD COLUMN IF NOT EXISTS key_card VARCHAR(255),               -- Идентификатор карты
    ADD COLUMN IF NOT EXISTS save_card BOOLEAN DEFAULT FALSE,     -- Признак необходимости сохранения карты
    ADD COLUMN IF NOT EXISTS recurrent BOOLEAN DEFAULT FALSE;     -- Признак рекуррентного платежа