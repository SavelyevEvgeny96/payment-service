package ru.sogaz.site.paymentService.constants

/**
 * Константы для сообщений об ошибках.
 * Это помогает централизованно управлять всеми сообщениями об ошибках.
 */
object ErrorMessages {
    const val INVALID_EMAIL_FORMAT = "Неверный формат email получателя"
    const val INVALID_PHONE_FORMAT = "Неверный формат телефона получателя"
    const val POLICYHOLDER_NAME_LENGTH = "Имя страхователя должно быть от 2 до 30 символов"
    const val INVALID_EXTERNAL_SYSTEM_CODE = "Неверный код внешней системы"
    const val PAYMENT_END_DATE_PAST = "Дата окончания действия ссылки на оплату не может быть в прошлом"
    const val INVALID_DATE_FORMAT = "Дата окончания действия ссылки на оплату не соответствует требуемому формату"
    const val INVALID_BANK = "Неверное значение банка, должно быть 'gpb'"
    const val INVALID_TOKEN = "Токен должен начинаться с 'Bearer '"
    const val UNAUTHORIZED = "Неавторизованный доступ"
}
