
package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.*
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.siter.models.resonses.Response

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
class PaymentServiceImpl(
    private val configDataRepository: ConfigDataRepository,
    private val apiConfigProperty: ApiConfigProperty,
    private val bankRepository: BankRepository,
    private val clientSystemRepository: ClientSystemRepository,
    private val orderRepository: OrderRepository,
    private val orderStatusRepository: OrderStatusRepository,
    private val subOrderRepository: SubOrderRepository,
) : PaymentService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_OVERDUE = "OVERDUE"
        const val STATUS_MARKEDDEL = "MARKEDDEL"
        const val LOG_START_PAYMENT_CREATION = "Начало создания платежа для TraiceId: {}"
        const val LOG_CODE_LENGTH_NOT_FOUND = "Не найдено значение длины для генерации Order.code "
        const val LOG_NOT_FOUND_ORDER_TO_CODE = "Ошибка совершения платежа. Указанный заказ (идентификатор/code заказа) не найден"
        const val LOG_ORDER_STATUS_SUCCESS = "Ошибка совершения платежа. Указанный заказ уже оплачен для TraceId: {}"
        const val LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL = "Ошибка совершения платежа. Указанный заказ не доступен для оплаты для TraceId: {} "
    }


    override fun createPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String
    ): ResponseEntity<Response<DataPay>> {
        logger.info(LOG_START_PAYMENT_CREATION + traceId)

        val orderFindByCode = try {
            orderRepository.findByCode(paymentPayRequest.code)
        } catch (e: Exception) {
            logger.error(e, LOG_NOT_FOUND_ORDER_TO_CODE, paymentPayRequest.code, traceId)
            throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND, traceId)
        }

        val orderStatus = orderFindByCode.orderStatus
        if (orderStatus != null) {
            if (orderStatus.stateId == STATUS_SUCCESS) {
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_SUCCESS + traceId)
                
                throw BusinessException(CODE_ERROR_ORDER_IS_PAID_FOR, traceId)
            }
            if (orderStatus.stateId == STATUS_OVERDUE || orderStatus.stateId == STATUS_MARKEDDEL)
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL + traceId)

            throw BusinessException(CODE_ERROR_ORDER_IS_NOT_AVAILABLE, traceId)
        }
        return r

    }
}