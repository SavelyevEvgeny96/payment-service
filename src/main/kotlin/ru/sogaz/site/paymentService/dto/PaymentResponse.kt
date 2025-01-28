package ru.sogaz.site.paymentService.dto

/**
 * DTO для ответа с данными о платеже.
 *
 * @property status Статус обработки платежа
 * @property code Уникальный код платежа
 * @property url Ссылка на оплату
 */
data class PaymentResponse(
    val traceId: String,
) {
    var status: String = ""
    var code: Int? = null
    val innerError: String = ""
    val messageError: String=""
    val errorsValidate: List<String>? = null
    var data: Data = Data()
}

class Data {
    var code: String = ""
    var url: String = ""
}