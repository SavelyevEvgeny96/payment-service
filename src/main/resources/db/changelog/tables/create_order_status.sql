CREATE TABLE order_status (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    state_id VARCHAR(50) NOT NULL,    -- Код статуса
    state_name VARCHAR(255) NOT NULL  -- Наименование статуса
);
INSERT INTO order_status(state_id,state_name)VALUE("0","Заказ создан")
INSERT INTO order_status(state_id,state_name)VALUE("1","Заказ актуализирован (ранее по данному полису уже была сгенерирована ссылка, в результате запроса данные по ней обновлены)")
INSERT INTO order_status(state_id,state_name)VALUE("2","Заказ просрочен")
INSERT INTO order_status(state_id,state_name)VALUE("3","Заказ помечен на удаление")
INSERT INTO order_status(state_id,state_name)VALUE("4","Заказ оплачен")

