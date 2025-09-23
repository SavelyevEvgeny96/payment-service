package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentTypeDao
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentRepository
import java.util.UUID

class PaymentDaoImpl(
    private val paymentRepository: PaymentRepository,
    private val paymentTypeDao: PaymentTypeDao,
) : PaymentDao {
    companion object {
        const val LOG_ERROR_GET_PAYMENT_BY_ORDER_ID = "Не найден платеж по данному TraceId: "
        const val LOG_ERROR_GET_PAYMENT_SAVE = "Не удалось сохранить данные платежа"
        const val ERROR_GET_PAYMENT_BY_ORDER_ID = "Ошибка поиска платежа по order_id Exception:  "
        const val PAYMENT_NOT_FOUND = "Ошибка запроса смены статуса. Указанный ордер не найден"
        const val LOG_ERROR_PAYMENT_FIND = "Не удалось найти платеж по данному bankId"
        const val ERROR_UPDATE_PAYMENT_RECORD = "Ошибка обновления платежа payment_id == null"
    }

    private val logger = loggerFor(javaClass)

    override fun getPayment(
        traceId: String,
        paymentId: UUID,
    ): Payment? =
        try {
            paymentRepository.findById(paymentId).orElse(null)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_GET_PAYMENT_BY_ORDER_ID, traceId)
            throw InnerException(traceId, "$ERROR_GET_PAYMENT_BY_ORDER_ID ${e.message}")
        }

    override fun getPaymentFromBankId(bankId: String): Payment =
        try {
            paymentRepository.findByPaymentBankId(bankId)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_GET_PAYMENT_BY_ORDER_ID)
            throw InnerException(getTraceId(), "$PAYMENT_NOT_FOUND ${e.message}")
        }

    override fun findByPaymentBankId(paymentId: String): Payment =
        try {
            paymentRepository.findByPaymentBankId(paymentId)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_PAYMENT_FIND)
            throw InnerException(getTraceId(), "$LOG_ERROR_PAYMENT_FIND ${e.message}")
        }

    override fun save(payment: Payment): Payment =
        try {
            paymentRepository.save(payment)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_GET_PAYMENT_SAVE)
            throw InnerException(getTraceId(), "$LOG_ERROR_GET_PAYMENT_SAVE ${e.message}")
        }

    override fun findPaymentType(paymentType: PaymentTypeEnum) = paymentTypeDao.findByTypeId(paymentType.value)
}
