package ru.sogaz.site.paymentService.enums

enum class ActionType(
    val value: String,
) {
    SAVE_ORDER("Сохранение заказа"),
    UPDATE_ORDER("Обновление заказа"),
    REDIRECT_TO_BANK("Пользователь перенаправлен на страницу банка"),
    DELETE_ORDER("Удаление заказа"),
    PAYMENT_ERROR("Ошибка при совершении платежа"),
    ORDER_PAID("Заказ оплачен"),
    PAYMENT_START("Старт платежа"),
    GET_ACCESS_TOKEN("Получение токена доступа"),
    GET_ACCESS_TOKEN_ERROR("Ошибка при получении токена доступа"),
    ACCESS_TOKEN_RECEIVED("Получен токен доступа"),
    SEND_PAYMENT_START_REQUEST("Отправка запроса для старта платежа"),
    PAYMENT_START_REQUEST_ERROR("Ошибка при отправке запроса на старт платежа"),
    PAYMENT_START_REQUEST_ERROR_AKB_BANK("Ошибка при отправке запроса на регистрацию заказа в АКБ Россия"),
    TRANSACTION_STATUS_RECEIVED("Получен статус транзакции"),
    GET_PAYMENT_LINK("Получение ссылки на оплату"),
    PAYMENT_LINK_REQUEST_ERROR("Ошибка при отправке запроса для получения платежной ссылки"),
    CALLBACK_RECEIVED("Получение CALLBACK от банка"),
    REGISTER_ORDER_AKB("Отправка запроса для регистрации заказа в АКБ Россия"),
}
