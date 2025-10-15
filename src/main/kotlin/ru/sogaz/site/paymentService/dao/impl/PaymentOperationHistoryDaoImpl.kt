package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository

class PaymentOperationHistoryDaoImpl(
    private val paymentOperationHistoryRepository: PaymentOperationHistoryRepository,
) : PaymentOperationHistoryDao {
    companion object {
        const val LOG_ERROR_PAYMENT_HISTORY_SAVE = "Не удалось сохранить историю "
    }

    private val logger = loggerFor(javaClass)

    override fun saveRecordOperationHistory(
        order: Order?,
        traceId: String,
        actionTypeName: String,
    ) {
        try {
            val operationHistory =
                PaymentOperationHistory(
                    action = actionTypeName,
                    order = order,
                )
            paymentOperationHistoryRepository.save(operationHistory)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_PAYMENT_HISTORY_SAVE)
            throw InnerException(RequestInfo.getTraceId(), "$LOG_ERROR_PAYMENT_HISTORY_SAVE ${e.message}")
        }
    }

    override fun save(
        order: Order,
        actionTypeName: String,
    ): PaymentOperationHistory =
        PaymentOperationHistory(
            action = actionTypeName,
            order = order,
        ).run(paymentOperationHistoryRepository::save)
}
