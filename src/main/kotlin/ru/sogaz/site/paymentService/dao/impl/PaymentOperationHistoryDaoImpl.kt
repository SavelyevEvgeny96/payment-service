package ru.sogaz.site.paymentService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository

@Repository
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
            logger.error(LOG_ERROR_PAYMENT_HISTORY_SAVE, e)
            throw InnerException(getTraceId(), "$LOG_ERROR_PAYMENT_HISTORY_SAVE ${e.message}")
        }
    }

    override fun saveForOrder(
        order: Order,
        actionTypeName: String,
    ): PaymentOperationHistory =
        PaymentOperationHistory(
            action = actionTypeName,
            order = order,
        ).run(paymentOperationHistoryRepository::save)
}
