drop table payment_status cascade;

-- select unnest(enum_range(null::payment_statuses_enum)); просмотр значений в базе
CREATE TYPE payment_status_enum as ENUM (
    'NEW',
    'REG',
    'WAIT',
    'SUCCESS',
    'FAIL',
    'REFUND',
    'DECLINED',
    'CALLBACK'
);

CREATE CAST (varchar AS payment_status_enum) WITH INOUT AS IMPLICIT;

create table payment_status_descriptions (
	status payment_status_enum PRIMARY KEY,
	comment VARCHAR(255) NOT NULL
);

INSERT INTO payment_status_descriptions(status,comment)VALUES('NEW','Создана запись о платеже');
INSERT INTO payment_status_descriptions(status,comment)VALUES('REG','Платеж зарегистрирован');
INSERT INTO payment_status_descriptions(status,comment)VALUES('WAIT','Платеж в обработке');
INSERT INTO payment_status_descriptions(status,comment)VALUES('SUCCESS','Успешная оплата');
INSERT INTO payment_status_descriptions(status,comment)VALUES('FAIL','Неуспешная оплата');
INSERT INTO payment_status_descriptions(status,comment)VALUES('REFUND','Оплата завершена успешно, а далее был выполнен возврат средств или отмена');
INSERT INTO payment_status_descriptions(status,comment)VALUES('DECLINED','Клиент отказался от оплаты');
INSERT INTO payment_status_descriptions(status,comment)VALUES('CALLBACK','Получение CALLBACK от Банка');
