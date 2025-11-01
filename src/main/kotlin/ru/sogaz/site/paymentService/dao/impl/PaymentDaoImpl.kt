package ru.sogaz.site.paymentService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentRepository

@Repository
class PaymentDaoImpl(
    private val paymentRepository: PaymentRepository,
) : PaymentDao {
    companion object {
        const val LOG_ERROR_GET_PAYMENT_BY_ORDER_ID = "Платеж не найден"
        const val LOG_ERROR_GET_PAYMENT_SAVE = "Не удалось сохранить данные платежа"
        const val ERROR_GET_PAYMENT_BY_ORDER_ID = "Ошибка поиска платежа по order_id Exception:  "
        const val PAYMENT_NOT_FOUND = "Ошибка запроса смены статуса. Указанный ордер не найден"
        const val LOG_ERROR_PAYMENT_FIND = "Не удалось найти платеж по данному bankId"
        const val ERROR_UPDATE_PAYMENT_RECORD = "Ошибка обновления платежа payment_id == null"
    }

    private val logger = loggerFor(javaClass)

    override fun getPaymentFromBankId(bankId: String): Payment =
        try {
            paymentRepository.findByPaymentBankId(bankId)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_GET_PAYMENT_BY_ORDER_ID, e)
            throw InnerException(getTraceId(), "$PAYMENT_NOT_FOUND ${e.message}")
        }

    override fun findByPaymentBankId(paymentId: String): Payment =
        try {
            paymentRepository.findByPaymentBankId(paymentId)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_PAYMENT_FIND, e)
            throw InnerException(getTraceId(), "$LOG_ERROR_PAYMENT_FIND ${e.message}")
        }

    override fun save(payment: Payment): Payment =
        try {
            paymentRepository.save(payment)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_GET_PAYMENT_SAVE, e)
            throw InnerException(getTraceId(), "$LOG_ERROR_GET_PAYMENT_SAVE ${e.message}")
        }

    override fun findByStatuses(statuses: List<PaymentStatusEnum>): List<Payment> = paymentRepository.findByStatuses(statuses)
}
