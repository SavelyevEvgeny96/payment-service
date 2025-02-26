
CREATE TABLE action_type (
    id BIGSERIAL PRIMARY KEY,         -- Автоинкрементируемый ID
    action_name VARCHAR(255) NOT NULL  -- Наименование действия
);
INSERT INTO action_type(action_name)VALUES('Сохранение заказа');
INSERT INTO action_type(action_name)VALUES('Обновление заказа');
INSERT INTO action_type(action_name)VALUES('Пользователь перенаправлен на страницу банка');
INSERT INTO action_type(action_name)VALUES('Удаление заказа');
INSERT INTO action_type(action_name)VALUES('Ошибка при совершени платежа');
INSERT INTO action_type(action_name)VALUES('Заказ оплачен');
INSERT INTO action_type(action_name)VALUES('Старт платежа');
INSERT INTO action_type(action_name)VALUES('Получение токена доступа');
INSERT INTO action_type(action_name)VALUES('Ошибка при получении токена доступа');
INSERT INTO action_type(action_name)VALUES('Получен токен доступа');
INSERT INTO action_type(action_name)VALUES('Отправка запроса для старта платежа');
INSERT INTO action_type(action_name)VALUES('Ошибка при отправке запроса на старт платежа');
INSERT INTO action_type(action_name)VALUES('Получен статус транзакции');
INSERT INTO action_type(action_name)VALUES('Заказ оплачен');