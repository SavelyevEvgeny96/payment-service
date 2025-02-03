
CREATE TABLE action_type (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    action_name VARCHAR(255) NOT NULL  -- Наименование действия
);
INSERT INTO action_type(action_name)VALUE("Сохранение заказа")
INSERT INTO action_type(action_name)VALUE("Обновление заказа")
INSERT INTO action_type(action_name)VALUE("Пользователь перенаправлен на страницу банка")
INSERT INTO action_type(action_name)VALUE("Удаление заказа")
INSERT INTO action_type(action_name)VALUE("Ошибка при совершени платежа")
INSERT INTO action_type(action_name)VALUE("Заказ оплачен")
INSERT INTO action_type(action_name)VALUE("Старт платежа")
INSERT INTO action_type(action_name)VALUE("Получение токена доступа")