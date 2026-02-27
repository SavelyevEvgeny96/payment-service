package ru.sogaz.site.paymentService.model.v2.exception

import java.util.UUID

private const val OPERATION_NOT_FOUND = "Операция по заказу [%s] с таким PaymentBankId: %s не найдена"

class OperationNotFoundException(
    orderId: UUID,
    paymentBankId: String,
) : Exception(OPERATION_NOT_FOUND.format(orderId, paymentBankId))
