package ru.sogaz.site.paymentService.service.impl

import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperty
import ru.sogaz.site.paymentService.repository.*
import ru.sogaz.site.paymentService.service.PaymentService

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


//    override fun createPayment(
//        paymentPayRequest: PaymentPayRequest,
//        traceId: String
//    ): ResponseEntity<Response<DataPay>> {
//        logger.info(LOG_START_PAYMENT_CREATION + traceId)
//
//        val orderFindByCode = try {
//            orderRepository.findByCode(paymentPayRequest.code)
//        } catch (e: Exception) {
//            logger.error(e, LOG_NOT_FOUND_ORDER_TO_CODE, paymentPayRequest.code, traceId)
//            throw BusinessException(CODE_ERROR_MAKING_PAYMENT, traceId)
//        }
//
//        val orderStatus = orderFindByCode.orderStatus
//        if (orderStatus != null) {
//            if (orderStatus.stateId == STATUS_SUCCESS) {
//                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_SUCCESS + traceId)
//                //уточнить касаемо обработки ошибки кода 409 с разными эрор меседжами как это сделать
//                throw BusinessException(CODE_ERROR_MAKING_PAYMENT, traceId)
//            }
//            if (orderStatus.stateId == STATUS_OVERDUE || orderStatus.stateId == STATUS_MARKEDDEL)
//                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL + traceId)
//            //уточнить касаемо обработки ошибки кода 409 с разными эрор меседжами как это сделать
//            throw BusinessException(CODE_ERROR_MAKING_PAYMENT, traceId)
//        }
//        return r
//
//    }
}


