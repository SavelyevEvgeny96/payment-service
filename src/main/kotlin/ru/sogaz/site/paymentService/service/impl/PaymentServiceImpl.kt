package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.*
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl.Companion.STATUS_CODE_SUCCESS
import ru.sogaz.siter.models.resonses.Response

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val paymentTypeRepository: PaymentTypeRepository,
    private val paymentStatusRepository: PaymentStatusRepository,
    private val configDataDao: ConfigDataDao,
    private val actionTypeRepository: ActionTypeRepository,
    private val configDataRepository: ConfigDataRepository,
    private val orderRepository: OrderRepository,
    private val subOrderRepository: SubOrderRepository,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
) : PaymentService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val PAYMENT_TYPE_PAY = "bankCard"
        const val PAYMENT_STATUS_NEW = "NEW"
        const val DESC = "Оплата для заказа с code{} : "
        const val GET_TOKEN_MASSAGE_SUCCESS = "Получен токен доступа"
        const val RUB = "RUB"
        const val PAYMENT_PREFIX = "/payment/"
        const val START_PREFIX = "/start"
        const val TOKEN_PREFIX = "/token"
        const val GPB_TOKEN_ROW = "token"
        const val TRUE = "true"
        const val BANK_PRIORITY_CHECK = "bankPriorityCheck"
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_OVERDUE = "OVERDUE"
        const val STATUS_MARKEDDEL = "MARKEDDEL"
        const val LOG_AND_ERROR_FIND_SUB_ORDER = "Ошибка получения SubOrder c code: "
        const val LOG_AND_ERROR_FIND_ACTION_TYPE = "Ошибка при получении action_name "
        const val LOG_START_PAYMENT_CREATION = "Начало создания платежа для TraiceId: {}"
        const val LOG_AND_ERROR_GET_PAYMENT_STATUS =
            "Ошибка получения статуса оплаты из таблицы payment_status для TraceID: "
        const val LOG_AND_ERROR_GET_TYPE_STATUS =
            "Ошибка при получении статуса типа оплаты из таблицы payment_type для TraceID:"
        const val LOG_NOT_FOUND_ORDER_TO_CODE =
            "Ошибка совершения платежа. Указанный заказ (идентификатор/code заказа) не найден"
        const val LOG_ORDER_STATUS_SUCCESS = "Ошибка совершения платежа. Указанный заказ уже оплачен для TraceId: {}"
        const val LOG_ERROR_UPDATE_ORDER_BY_CODE = "Обновление полей urlToReturn и urlToDecline в  заявке не выполненно"
        const val ERROR_UPDATE_ORDER_BY_CODE = "Ошибка Обновления полей urlToReturn и urlToDecline для заявки с code: "
        const val LOG_ERROR_BANK_PRIORITY_CHECK =
            "Параметр с paramName \"bankPriorityCheck\"  не найден в конфигурационной таблице"
        const val LOG_ERROR_BANK_PRIORITY =
            "Параметр с paramName \"bankPriority\"  не найден в конфигурационной таблице"

        const val LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL =
            "Ошибка совершения платежа. Указанный заказ не доступен для оплаты для TraceId: {} "
        const val ERROR_BANK_PRIORITY_CHECK = "Ошибка поиска параметра \"bankPriorityCheck\""
    }

    override fun createPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
    ): ResponseEntity<Response<DataPay>> {
        logger.info(LOG_START_PAYMENT_CREATION + traceId)

        val orderFindByCode =
            try {
                orderRepository.findByCode(paymentPayRequest.code)
            } catch (e: Exception) {
                logger.error(e, LOG_NOT_FOUND_ORDER_TO_CODE, paymentPayRequest.code, traceId)
                throw BusinessException(CODE_ERROR_ORDER_NOT_FOUND, traceId)
            }

        val premiumAmount = orderFindByCode.premiumAmount
        val orderStatus = orderFindByCode.orderStatus

        if (orderStatus != null) {
            if (orderStatus.stateId == STATUS_SUCCESS) {
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_SUCCESS + traceId)
                throw BusinessException(CODE_ERROR_ORDER_IS_PAID_FOR, traceId)
            }
            if (orderStatus.stateId == STATUS_OVERDUE || orderStatus.stateId == STATUS_MARKEDDEL) {
                logger.error(orderStatus.stateId + LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL + traceId)
                throw BusinessException(CODE_ERROR_ORDER_IS_NOT_AVAILABLE, traceId)
            }
        }

        orderFindByCode.urlToReturn = paymentPayRequest.urlToReturn
        orderFindByCode.urlToDecline = paymentPayRequest.urlToReturnF

        try {
            orderRepository.save(orderFindByCode)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_UPDATE_ORDER_BY_CODE, traceId)
            throw InnerException(traceId, ERROR_UPDATE_ORDER_BY_CODE + orderFindByCode.code)
        }
        val configBankPriorityCheck =
            try {
                configDataRepository.findByParamName(BANK_PRIORITY_CHECK)
            } catch (e: Exception) {
                logger.error(e, LOG_ERROR_BANK_PRIORITY_CHECK, traceId)
                throw InnerException(traceId, ERROR_BANK_PRIORITY_CHECK)
            }
        val bank = orderFindByCode.bankId
        val checkBank = configDataDao.getBank(bank?.bankId, traceId)
        if (configBankPriorityCheck.paramValue == TRUE || checkBank != null) {
            val tokenGpb = configDataDao.getGPBToken(traceId, orderFindByCode)

            if (tokenGpb.isNotEmpty()) {
                val actionTypeTokenSuccess =
                    try {
                        actionTypeRepository.findByActionName(GET_TOKEN_MASSAGE_SUCCESS)
                    } catch (e: Exception) {
                        logger.error(e, LOG_AND_ERROR_FIND_ACTION_TYPE, traceId)
                        throw InnerException(traceId, LOG_AND_ERROR_FIND_ACTION_TYPE)
                    }
                val subOrder =
                    try {
                        subOrderRepository.findFirstByOrderId(orderFindByCode)
                    } catch (e: Exception) {
                        logger.error(e, LOG_AND_ERROR_FIND_SUB_ORDER, orderFindByCode.code)
                        throw InnerException(traceId, "$LOG_AND_ERROR_FIND_SUB_ORDER$orderFindByCode")
                    }
                val operationHistory =
                    PaymentOperationHistory(
                        action = actionTypeTokenSuccess,
                        order = orderFindByCode,
                        actionAuthor = subOrder.clientSystem,
                        actionDate = null,
                    )
                operationHistoryRepository.save(operationHistory)

                val paymentStatusNEW =
                    try {
                        paymentStatusRepository.findByStateId(PAYMENT_STATUS_NEW)
                    } catch (e: Exception) {
                        logger.error(e, LOG_AND_ERROR_GET_PAYMENT_STATUS, traceId)
                        throw InnerException(traceId, LOG_AND_ERROR_GET_PAYMENT_STATUS)
                    }
                val paymentType =
                    try {
                        paymentTypeRepository.findByTypeId(PAYMENT_TYPE_PAY)
                    } catch (e: Exception) {
                        logger.error(e, LOG_AND_ERROR_GET_TYPE_STATUS, traceId)
                        throw InnerException(traceId, LOG_AND_ERROR_GET_TYPE_STATUS)
                    }
                val paymentRecord = Payment(
                    bank = checkBank,
                    stateId = paymentStatusNEW,
                    orderId = orderFindByCode,
                    typeId = paymentType,
                )
                paymentRepository.save(paymentRecord)

            }


            val resultResponseGPB =
                configDataDao.initiateGPBPayment(paymentPayRequest, traceId, tokenGpb, premiumAmount, orderFindByCode)




            return resultResponseGPB
        }
        val response =
            Response(
                status = OrderServiceImpl.SUCCESS,
                code = STATUS_CODE_SUCCESS,
                traceId = traceId,
                data = DataPay(""),
            )
        return ResponseEntity.ok(response)
    }
}
