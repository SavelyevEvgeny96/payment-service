drop table order_status cascade;

-- select unnest(enum_range(null::order_statuses_enum)); просмотр значений в базе
CREATE TYPE order_status_enum as ENUM (
    'NEW',
    'UPDATE',
    'OVERDUE',
    'MARKEDDEL',
    'SUCCESS'
);

CREATE CAST (varchar AS order_status_enum) WITH INOUT AS IMPLICIT;

create table order_status_descriptions (
	status order_status_enum PRIMARY KEY,
	comment VARCHAR(255) NOT NULL
);

INSERT INTO order_status_descriptions(status,comment)VALUES('NEW','Заказ создан');
INSERT INTO order_status_descriptions(status,comment)VALUES('UPDATE','Заказ актуализирован (ранее по данному полису уже была сгенерирована ссылка, в результате запроса данные по ней обновлены)');
INSERT INTO order_status_descriptions(status,comment)VALUES('OVERDUE','Заказ просрочен');
INSERT INTO order_status_descriptions(status,comment)VALUES('MARKEDDEL','Заказ помечен на удаление');
INSERT INTO order_status_descriptions(status,comment)VALUES('SUCCESS','Заказ оплачен');