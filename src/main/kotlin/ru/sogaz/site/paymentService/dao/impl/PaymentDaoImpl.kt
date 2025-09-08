package ru.sogaz.site.paymentService.dao.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.dto.data.DataPaymentUpdate
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.ERROR_UPDATE_PAYMENT_RECORD
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.PAYMENT_STATUS_REG

class PaymentDaoImpl(
    private val paymentRepository: PaymentRepository,
    private val paymentStatusDao: PaymentStatusDao,
) : PaymentDao {
    companion object {
        const val LOG_ERROR_GET_PAYMENT_BY_ORDER_ID = "Не найден платеж по данному TraceId: "
        const val LOG_ERROR_GET_PAYMENT_SAVE = "Не удалось сохранить данные платежа"
        const val ERROR_GET_PAYMENT_BY_ORDER_ID = "Ошибка поиска платежа по order_id Exception:  "
        const val PAYMENT_NOT_FOUND = "Ошибка запроса смены статуса. Указанный ордер не найден"
        const val LOG_ERROR_PAYMENT_FIND = "Не удалось найти платеж по данному bankId"
    }

    private val logger = loggerFor(javaClass)

    override fun getPayment(
        traceId: String,
        paymentId: Long,
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

    override fun save(payment: Payment): Long? {
        val saved =
            try {
                paymentRepository.save(payment)
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_GET_PAYMENT_SAVE)
                throw InnerException(getTraceId(), "$LOG_ERROR_GET_PAYMENT_SAVE ${e.message}")
            }
        return saved.id
    }

    override fun findByPaymentBankId(paymentId: String): Payment =
        try {
            paymentRepository.findByPaymentBankId(paymentId)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_PAYMENT_FIND)
            throw InnerException(getTraceId(), "$LOG_ERROR_PAYMENT_FIND ${e.message}")
        }

    override fun paymentUpdate(dataPaymentUpdate: DataPaymentUpdate) {
        val traceId = getTraceId()
        if (dataPaymentUpdate.paymentId != null) {
            val getPaymentForUpdate = getPayment(traceId, dataPaymentUpdate.paymentId)
            val paymentStatusREG = paymentStatusDao.getPaymentStatus(traceId, PAYMENT_STATUS_REG)
            getPaymentForUpdate?.paymentPageUrl = dataPaymentUpdate.paymentPageUrl
            getPaymentForUpdate?.stateId = paymentStatusREG
            getPaymentForUpdate?.qrcId = dataPaymentUpdate.qtcId
            getPaymentForUpdate?.paymentBankId = dataPaymentUpdate.paymentBankId
            if (getPaymentForUpdate != null) {
                paymentRepository.save(getPaymentForUpdate)
            }
        } else {
            logger.error(ERROR_UPDATE_PAYMENT_RECORD)
            throw InnerException(traceId, ERROR_UPDATE_PAYMENT_RECORD)
        }
    }
}
