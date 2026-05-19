package ru.sogaz.site.paymentService.model.v2.exception

private const val OPERATION_NOT_FOUND = "Операция с таким PaymentBankId: %s не найдена"

class OperationNotFoundException(
    paymentBankId: String,
) : Exception(OPERATION_NOT_FOUND.format(paymentBankId))
