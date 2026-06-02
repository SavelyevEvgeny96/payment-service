package ru.sogaz.site.paymentService.service.payment

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.payment.receipt.client.api.PaymentReceiptControllerApi
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.ChequeStateEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.receipt.ReceiptMapper
import ru.sogaz.site.paymentService.service.ReceiptService

@Service
class ReceiptServiceImpl(
    private val paymentDao: PaymentDao,
    private val receiptMapper: ReceiptMapper,
    private val paymentReceiptControllerApi: PaymentReceiptControllerApi,
) : ReceiptService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val LOG_RECEIPT_SUCCESS = "Чек успешно сгенерирован для заказа %s. TraceId: %s"
        const val LOG_RECEIPT_FAILED = "Ошибка при генерации чека. TraceId: %s"
        const val LOG_RECEIPT_API_ERROR = "Ошибка API при генерации чека. Status: %s. TraceId: %s"
        const val LOG_RECEIPT_ERROR = "Ошибка при генерации чека для заказа %s. TraceId: %s"
        const val ERROR_DATA_RECEIPT = "Ошибка данных чека"
        const val ERROR_RECEIPT = "Ошибка сервиса чеков: "
        const val ERROR_RECEIPT_GENERATION = "Ошибка при генерации чека: "
        const val ERROR_FRACTION_SUM = "Дробная часть должна содержать не более 2 знаков"
        const val ERROR_HOLL_SUM = "Целая часть должна содержать не более 8 знаков"
        const val ERROR_INCORRECT_SUM = "Некорректный формат суммы: "
    }

    override fun generateReceipt(payment: Payment) {
        val traceId = getTraceId()
        if (payment.chequeName.equals(ChequeStateEnum.SENT.name) && payment.state != PaymentStatusEnum.REFUND) {
            return
        }
        val orderId = payment.order.id
        val request = receiptMapper.mapFromPaymentToReceiptCreateRequest(payment)

        try {
            val response = paymentReceiptControllerApi.createPaymentCheck(request)

            when (response.status) {
                StatusEnum.SUCCESS.value -> {
                    logger.debug(LOG_RECEIPT_SUCCESS.format(orderId, traceId))
                    handleReceiptSuccess(payment)
                }

                StatusEnum.FAILED.value -> {
                    logger.error(LOG_RECEIPT_FAILED.format(traceId))
                    handleReceiptError(payment)
                    throw InnerException(traceId, ERROR_DATA_RECEIPT)
                }

                else -> {
                    logger.error(LOG_RECEIPT_API_ERROR.format(response.status, traceId))
                    handleReceiptError(payment)
                    throw InnerException(traceId, ERROR_RECEIPT + response.code)
                }
            }
        } catch (e: Exception) {
            logger.debug(LOG_RECEIPT_ERROR.format(orderId, traceId), e)
            if (payment.paymentBankId != null) {
                handleReceiptError(payment)
            }
            throw InnerException(traceId, ERROR_RECEIPT_GENERATION + e.message)
        }
    }

    private fun handleReceiptError(payment: Payment) {
        payment.chequeName = ChequeStateEnum.NOT_SENT.name
        paymentDao.save(payment)
    }

    private fun handleReceiptSuccess(payment: Payment) {
        payment.chequeName = ChequeStateEnum.SENT.name
        paymentDao.save(payment)
    }
}
