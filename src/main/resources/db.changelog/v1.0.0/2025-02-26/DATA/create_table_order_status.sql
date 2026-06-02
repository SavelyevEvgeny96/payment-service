CREATE TABLE order_status (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    state_id VARCHAR(50) UNIQUE ,    -- Код статуса
    state_name VARCHAR(255) NOT NULL -- Наименование статуса

);
INSERT INTO order_status(state_id,state_name)VALUES('NEW','Заказ создан');
INSERT INTO order_status(state_id,state_name)VALUES('UPDATE','Заказ актуализирован (ранее по данному полису уже была сгенерирована ссылка, в результате запроса данные по ней обновлены)');
INSERT INTO order_status(state_id,state_name)VALUES('OVERDUE','Заказ просрочен');
INSERT INTO order_status(state_id,state_name)VALUES('MARKEDDEL','Заказ помечен на удаление');
INSERT INTO order_status(state_id,state_name)VALUES('SUCCESS','Заказ оплачен');










