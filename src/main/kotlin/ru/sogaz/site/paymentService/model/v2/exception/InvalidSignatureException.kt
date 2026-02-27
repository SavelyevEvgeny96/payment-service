package ru.sogaz.site.paymentService.model.v2.exception

private const val ERROR_TRX_ID = "Произошла ошибка сертификата при проверке для callback-a по заказу: [%s] с PaymentBankId: %s"

class InvalidSignatureException(
    orderId: String,
    paymentBankId: String,
) : Exception(ERROR_TRX_ID.format(orderId, paymentBankId))
