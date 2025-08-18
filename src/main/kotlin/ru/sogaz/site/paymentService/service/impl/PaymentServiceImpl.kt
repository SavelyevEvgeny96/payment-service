package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR_SBP
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.GetPaymentTypeDao
import ru.sogaz.site.paymentService.dao.GetSubOrderDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.PaymentContext
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.GazpromService
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.PaymentStatusCheckerService
import ru.sogaz.siter.models.resonses.Response

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
class PaymentServiceImpl(
    private val gazpromService: GazpromService,
    private val orderDao: OrderDao,
    private val paymentRepository: PaymentRepository,
    private val getPaymentTypeDao: GetPaymentTypeDao,
    private val getPaymentStatusDao: GetPaymentStatusDao,
    private val getActionTypeDao: GetActionTypeDao,
    private val orderRepository: OrderRepository,
    private val getSubOrderDao: GetSubOrderDao,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val paymentStatusCheckerService: PaymentStatusCheckerService,
    private val bankDao: BankDao,
) : PaymentService {
    private val logger = loggerFor(javaClass)

    companion object {
        const val PAYMENT_TYPE_PAY = "bankCard"
        const val GET_TOKEN_MASSAGE_SUCCESS = "Получен токен доступа"
        const val RUB = "RUB"
        const val PAYMENT_PREFIX = "/payment/"
        const val START_PREFIX = "/start"
        const val TOKEN_PREFIX = "/token"
        const val GPB_TOKEN_ROW = "token"
        const val TRUE = "true"
        const val LOG_AND_ERROR_FIND_SUB_ORDER = "Ошибка получения SubOrder c code: "
        const val LOG_AND_ERROR_FIND_ACTION_TYPE = "Ошибка при получении action_name "
        const val LOG_START_PAYMENT_RECORD = ">>> СТАРТ метода создание записи в таблице payments "
        const val LOG_END_PAYMENT_RECORD = "<<< КОНЕЦ метода создание записи в таблице payments для payment_id: "
        const val LOG_START_PAYMENT_CREATION = "***** Начало ***** создания платежа по карте для TraceId: "
        const val LOG_START_PAYMENT_CREATION_SBP = "***** Начало ***** создания платежа по СБП для TraceId: "
        const val LOG_AND_ERROR_GET_PAYMENT_STATUS =
            "Ошибка получения статуса оплаты из таблицы payment_status для TraceID: "
        const val LOG_AND_ERROR_GET_TYPE_STATUS =
            "Ошибка при получении статуса типа оплаты из таблицы payment_type для TraceID:"
        const val LOG_NOT_FOUND_ORDER_TO_CODE =
            "Ошибка совершения платежа. Указанный заказ (идентификатор/code заказа) не найден"
        const val LOG_ORDER_STATUS_SUCCESS = "Ошибка совершения платежа. Указанный заказ уже оплачен для TraceId: {}"
        const val LOG_ERROR_UPDATE_ORDER_BY_CODE = "Обновление полей urlToReturn и urlToDecline в  заявке не выполненно"
        const val ERROR_UPDATE_ORDER_BY_CODE = "Ошибка Обновления полей urlToReturn и urlToDecline для заявки с orderId: "
        const val LOG_ORDER_STATUS_OVERDUE_OR_MARKEDDEL =
            "Ошибка совершения платежа. Указанный заказ не доступен для оплаты для TraceId: {} "
    }

    override fun createPayment(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
    ): ResponseEntity<Response<DataPay>> {
        logger.info("$LOG_START_PAYMENT_CREATION $traceId")
        val paymentContext =
            buildPaymentContext(
                paymentPayRequest,
                CODE_ERROR_ORDER_IS_PAID_FOR,
                CODE_ERROR_ORDER_IS_NOT_AVAILABLE,
                traceId,
            )
        val checkBank = paymentContext.checkBank
        val orderFindByCode = paymentContext.order
        val subOrder = paymentContext.subOrder
        if (paymentContext.configBankPriorityCheck == TRUE || checkBank != null) {
            val tokenGpb = gazpromService.getGPBToken(traceId, orderFindByCode, subOrder)
            if (tokenGpb.isNotEmpty()) {
                val actionTypeTokenSuccess = getActionTypeDao.getActionType(traceId, GET_TOKEN_MASSAGE_SUCCESS)
                val operationHistory =
                    PaymentOperationHistory(
                        action = actionTypeTokenSuccess,
                        order = orderFindByCode,
                        actionAuthor = paymentContext.subOrder.clientSystem,
                        actionDate = null,
                    )
                operationHistoryRepository.save(operationHistory)
            }
            val paymentId = createPaymentRecord(checkBank, orderFindByCode, tokenGpb, traceId)
            return gazpromService.initiateGPBPayment(
                paymentPayRequest,
                traceId,
                tokenGpb,
                paymentId,
                paymentContext.premiumAmount,
                orderFindByCode,
                subOrder,
            )
        }
        throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
    }

    override fun createPaymentSbp(
        paymentPayRequest: PaymentPayRequest,
        traceId: String,
    ): ResponseEntity<Response<DataPay>> {
        logger.info("$LOG_START_PAYMENT_CREATION_SBP + $traceId")
        var paymentId: Long? = null
        val paymentContext =
            buildPaymentContext(
                paymentPayRequest,
                CODE_ERROR_ORDER_IS_PAID_FOR_SBP,
                CODE_ERROR_ORDER_IS_NOT_AVAILABLE,
                traceId,
            )
        val checkBank = paymentContext.checkBank
        val orderFindByCode = paymentContext.order
        val subOrder = paymentContext.subOrder
        if (paymentContext.configBankPriorityCheck == TRUE || checkBank != null) {
            paymentId = createPaymentRecord(checkBank, orderFindByCode, null, traceId)
        }
        return gazpromService.initiateGPBSBPPayment(
            paymentPayRequest,
            traceId,
            paymentId,
            paymentContext.premiumAmount,
            orderFindByCode,
            subOrder,
        )
    }

    override fun buildPaymentContext(
        paymentPayRequest: PaymentPayRequest,
        errorCodeIsPaidFor: Int,
        errorCodeIsNotAvailable: Int,
        traceId: String,
    ): PaymentContext {
        val orderFindByOrderId = orderDao.getOrderId(traceId, paymentPayRequest.orderId)
        val subOrder = getSubOrderDao.getSubOrder(traceId, orderFindByOrderId)
        val premiumAmount = orderFindByOrderId.premiumAmount
        val orderStatus = orderFindByOrderId.orderStatus
        paymentStatusCheckerService.checkStatusOrder(orderStatus, errorCodeIsPaidFor, errorCodeIsNotAvailable, traceId)
        orderFindByOrderId.urlToReturn = paymentPayRequest.urlToReturn
        orderFindByOrderId.urlToDecline = paymentPayRequest.urlToReturnF
        try {
            orderRepository.save(orderFindByOrderId)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_UPDATE_ORDER_BY_CODE, traceId)
            throw InnerException(traceId, ERROR_UPDATE_ORDER_BY_CODE + orderFindByOrderId.orderId)
        }
        val configBankPriorityCheck = bankDao.getBankPriority(traceId)
        val bank = orderFindByOrderId.bankId
        val checkBank = bankDao.getBank(bank?.bankId, traceId)
        return PaymentContext(orderFindByOrderId, subOrder, premiumAmount, orderStatus, configBankPriorityCheck, checkBank)
    }

    private fun createPaymentRecord(
        checkBank: Bank?,
        order: Order,
        tokenGpb: String?,
        traceId: String,
    ): Long? {
        logger.info(LOG_START_PAYMENT_RECORD)
        val paymentStatus = getPaymentStatusDao.getPaymentStatus(traceId, StatusEnum.NEW.value)
        val paymentType = getPaymentTypeDao.getPaymentType(traceId, PAYMENT_TYPE_PAY)
        val paymentRecord =
            Payment(
                bank = checkBank,
                stateId = paymentStatus,
                orderId = order,
                typeId = paymentType,
                paymentBankId = tokenGpb,
            )
        val saved = paymentRepository.save(paymentRecord)
        logger.info("$LOG_END_PAYMENT_RECORD ${saved.id}")
        return saved.id
    }
}
