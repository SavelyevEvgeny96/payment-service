package ru.sogaz.site.paymentService.service.impl

import org.springframework.http.ResponseEntity
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR_SBP
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetBankDao
import ru.sogaz.site.paymentService.dao.GetBankPriorityDao
import ru.sogaz.site.paymentService.dao.GetOrderDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.dao.GetPaymentTypeDao
import ru.sogaz.site.paymentService.dao.GetSubOrderDao
import ru.sogaz.site.paymentService.dto.DataPay
import ru.sogaz.site.paymentService.dto.PaymentContext
import ru.sogaz.site.paymentService.dto.PaymentPayRequest
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentOperationHistory
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.GazpromService
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.util.Util
import ru.sogaz.siter.models.resonses.Response

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
class PaymentServiceImpl(
    private val gazpromService: GazpromService,
    private val getOrderDao: GetOrderDao,
    private val paymentRepository: PaymentRepository,
    private val getPaymentTypeDao: GetPaymentTypeDao,
    private val getPaymentStatusDao: GetPaymentStatusDao,
    private val getActionTypeDao: GetActionTypeDao,
    private val configDataRepository: ConfigDataRepository,
    private val orderRepository: OrderRepository,
    private val getSubOrderDao: GetSubOrderDao,
    private val operationHistoryRepository: PaymentOperationHistoryRepository,
    private val util: Util,
    private val getBankDao: GetBankDao,
    private val getBankPriorityDao: GetBankPriorityDao,
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
        const val PAYMENT_STATUS_NEW = "NEW"
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_OVERDUE = "OVERDUE"
        const val STATUS_MARKEDDEL = "MARKEDDEL"
        const val LOG_AND_ERROR_FIND_SUB_ORDER = "Ошибка получения SubOrder c code: "
        const val LOG_ERROR_UPDATE_PAYMENT_RECORD = "Ошибка обновления заявки оплаты для TraiceId:  "
        const val LOG_AND_ERROR_FIND_ACTION_TYPE = "Ошибка при получении action_name "
        const val LOG_START_PAYMENT_CREATION = "Начало создания платежа для TraiceId: "
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

    private val traceId = getTraceId()

    override fun createPayment(paymentPayRequest: PaymentPayRequest): ResponseEntity<Response<DataPay>> {
        val paymentContext =
            buildPaymentContext(paymentPayRequest, CODE_ERROR_ORDER_IS_PAID_FOR, CODE_ERROR_ORDER_IS_NOT_AVAILABLE)
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
                updatePaymentRecord(checkBank, PAYMENT_STATUS_NEW, PAYMENT_TYPE_PAY, orderFindByCode, tokenGpb)
                val paymentStatusNEW = getPaymentStatusDao.getPaymentStatus(traceId, PAYMENT_STATUS_NEW)
                val paymentTypePayCard = getPaymentTypeDao.getPaymentType(traceId, PAYMENT_TYPE_PAY)
                val paymentRecord =
                    Payment(
                        bank = checkBank,
                        stateId = paymentStatusNEW,
                        orderId = orderFindByCode,
                        typeId = paymentTypePayCard,
                        paymentBankId = tokenGpb,
                    )
                paymentRepository.save(paymentRecord)
            }
            return gazpromService.initiateGPBPayment(
                paymentPayRequest,
                traceId,
                tokenGpb,
                paymentContext.premiumAmount,
                orderFindByCode,
                subOrder,
            )
        }
        throw BusinessException(CustomPaymentErrors.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE, traceId)
    }

    override fun createPaymentSbp(paymentPayRequest: PaymentPayRequest): ResponseEntity<Response<DataPay>> {
        logger.info(LOG_START_PAYMENT_CREATION + traceId)
        val paymentContext =
            buildPaymentContext(paymentPayRequest, CODE_ERROR_ORDER_IS_PAID_FOR_SBP, CODE_ERROR_ORDER_IS_NOT_AVAILABLE)
        val checkBank = paymentContext.checkBank
        val orderFindByCode = paymentContext.order
        val subOrder = paymentContext.subOrder
        if (paymentContext.configBankPriorityCheck == TRUE || checkBank != null) {
            updatePaymentRecord(checkBank, PAYMENT_STATUS_NEW, PAYMENT_TYPE_PAY, orderFindByCode, null)
        }
        return gazpromService.initiateGPBSBPPayment(
            paymentPayRequest,
            traceId,
            paymentContext.premiumAmount,
            orderFindByCode,
            subOrder
        )
    }

    override fun buildPaymentContext(
        paymentPayRequest: PaymentPayRequest,
        errorCodeIsPaidFor: Int,
        errorCodeIsNotAvailable: Int,
    ): PaymentContext {
        logger.info(LOG_START_PAYMENT_CREATION + traceId)
        val orderFindByCode = getOrderDao.getOrderByCode(paymentPayRequest.code, traceId)
        val subOrder = getSubOrderDao.getSubOrder(traceId, orderFindByCode)
        val premiumAmount = orderFindByCode.premiumAmount
        val orderStatus = orderFindByCode.orderStatus
        util.checkStatusOrder(orderStatus, errorCodeIsPaidFor, errorCodeIsNotAvailable, traceId)
        orderFindByCode.urlToReturn = paymentPayRequest.urlToReturn
        orderFindByCode.urlToDecline = paymentPayRequest.urlToReturnF
        try {
            orderRepository.save(orderFindByCode)
        } catch (e: Exception) {
            logger.error(e, LOG_ERROR_UPDATE_ORDER_BY_CODE, traceId)
            throw InnerException(traceId, ERROR_UPDATE_ORDER_BY_CODE + orderFindByCode.code)
        }
        val configBankPriorityCheck = getBankPriorityDao.getBankPriority(traceId)
        val bank = orderFindByCode.bankId
        val checkBank = getBankDao.getBank(bank?.bankId, traceId)
        return PaymentContext(orderFindByCode, subOrder, premiumAmount, orderStatus, configBankPriorityCheck, checkBank)
    }

    private fun updatePaymentRecord(
        checkBank: Bank?, paymentStatusConstant: String, paymentTypeConstant: String, order: Order,
        tokenGpb: String?
    ) {
        try {
            val paymentStatus = getPaymentStatusDao.getPaymentStatus(traceId, paymentStatusConstant)
            val paymentType = getPaymentTypeDao.getPaymentType(traceId, paymentTypeConstant)
            val paymentRecord =
                Payment(
                    bank = checkBank,
                    stateId = paymentStatus,
                    orderId = order,
                    typeId = paymentType,
                    paymentBankId = tokenGpb,
                )
            paymentRepository.save(paymentRecord)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_UPDATE_PAYMENT_RECORD + traceId)
        }
    }
}
