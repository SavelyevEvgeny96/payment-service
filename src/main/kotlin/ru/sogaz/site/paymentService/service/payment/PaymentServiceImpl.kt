package ru.sogaz.site.paymentService.service.payment

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE_SBP
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR_SBP
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND_SBP
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_STATUS_BANK
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_UPDATED_ORDER_NOT_FOUND
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.data.GpbSbpHeadersParams
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.dto.request.PageInfoRequestParams
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.dto.response.ResponseStatusPay
import ru.sogaz.site.paymentService.dto.response.UpdatePaymentInvoiceResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.ChequeStateEnum
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.OrderMapper
import ru.sogaz.site.paymentService.orThrow
import ru.sogaz.site.paymentService.properties.ServiceStatuses.Companion.SUCCESS_STATUS_CODE_PAY_INFO_PAGE
import ru.sogaz.site.paymentService.properties.ServiceStatuses.Companion.SUCCESS_STATUS_CODE_UPDATE_PAYMENT_STATUS
import ru.sogaz.site.paymentService.service.InfoPageService
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.PaymentStatusService
import ru.sogaz.site.paymentService.service.RegisterPaymentService
import ru.sogaz.site.paymentService.service.SubOrderService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime
import java.util.UUID

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
@Service
class PaymentServiceImpl(
    private val orderDao: OrderDao,
    private val bankDao: BankDao,
    private val registerPaymentService: RegisterPaymentService,
    private val subOrderService: SubOrderService,
    private val orderMapper: OrderMapper,
    private val waitingPaymentDao: WaitingPaymentDao,
    private val infoPageService: InfoPageService,
    private val paymentStatusService: PaymentStatusService,
) : PaymentService {
    companion object {
        const val SUCCESS_UPDATE_CODE_PAYMENT_INVOICE = 1101580200

        const val SUCCESS_UPDATE_PAYMENT_INVOICE_MESSAGE = "ok"
        const val SBP_ACTIVE_CONFIG_NAME = "sbpActive"
        const val LOG_ORDER_INFO_BEFORE_REGISTRATION = "Для регистрации платежа по заказу с id: %s был выбран банк: %s"
        const val LOG_PAYMENT_INFO_AFTER_REGISTRATION =
            "Платеж по заказу с id: %s был успешно зарегистрирован в банке: %s"
        const val LOG_UPDATE_PAYMENT_INVOICE = "Начало обновления информации о заказе с orderId = "
        const val LOG_SUCCESS_UPDATE_PAYMENT_INVOICE = "Успешное обновление информации о заказе с orderId = "
    }

    private val logger = loggerFor(javaClass)

    override fun createCardPayment(
        orderId: UUID,
        urlToReturnS: String?,
        urlToReturnF: String?,
    ): DataPay =
        createPayment(
            orderId,
            PaymentTypeEnum.CARD,
            UrlToReturn(urlToReturnS, urlToReturnF),
        )

    override fun createSBPPayment(
        orderId: UUID,
        urlToReturnS: String?,
        urlToReturnF: String?,
        paymentDelay: String?,
        processPayments: String?,
        paymentStatus: String?,
    ): DataPay =
        createPayment(
            orderId,
            PaymentTypeEnum.SBP,
            UrlToReturn(urlToReturnS, urlToReturnF),
            GpbSbpHeadersParams(paymentDelay, processPayments, paymentStatus),
        )

    override fun getOrderPaymentPageInfo(
        orderId: UUID,
        pageInfoRequestParams: PageInfoRequestParams,
    ): Response<DataOrderPaymentPageInfo> =
        infoPageService
            .getInfo(orderId, pageInfoRequestParams)
            .wrapToSuccessResponse(SUCCESS_STATUS_CODE_PAY_INFO_PAGE)

    override fun updatePaymentInvoice(updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest): Response<UpdatePaymentInvoiceResponse> {
        logger.info(LOG_UPDATE_PAYMENT_INVOICE + updatePaymentInvoiceRequest.orderId)

        val existedOrderPayment =
            orderDao
                .findById(updatePaymentInvoiceRequest.orderId)
                .orThrow { BusinessException(CODE_ERROR_UPDATED_ORDER_NOT_FOUND, getTraceId()) }

        subOrderService.updateSubOrder(updatePaymentInvoiceRequest)

        val updatedOrder = orderMapper.updateOrder(updatePaymentInvoiceRequest, existedOrderPayment)
        orderDao.save(updatedOrder)

        logger.info(LOG_SUCCESS_UPDATE_PAYMENT_INVOICE + updatePaymentInvoiceRequest.orderId)
        return getSuccessResponse(
            getTraceId(),
            SUCCESS_UPDATE_CODE_PAYMENT_INVOICE,
            UpdatePaymentInvoiceResponse(SUCCESS_UPDATE_PAYMENT_INVOICE_MESSAGE),
        )
    }

    override fun updateStatus(paymentBankId: String): Response<ResponseStatusPay> =
        paymentBankId
            .run(paymentStatusService::updateStatus)
            .orThrow { throw BusinessException(CODE_ERROR_PAYMENT_STATUS_BANK) }
            .toResponseStatusPay()
            .wrapToSuccessResponse(SUCCESS_STATUS_CODE_UPDATE_PAYMENT_STATUS)

    private fun createPayment(
        orderId: UUID,
        paymentTypeEnum: PaymentTypeEnum,
        urlToReturn: UrlToReturn,
        headersParams: GpbSbpHeadersParams? = null,
    ): DataPay =
        orderDao
            .findById(orderId)
            .run { validateOrder(this, paymentTypeEnum) }
            .apply { bank = resolveCurrentBank(bank) }
            .run { registerPayment(this, paymentTypeEnum, urlToReturn, headersParams) }
            .also(::renewOrder)
            .also(waitingPaymentDao::saveWaitingForPayment)
            .toDataPay()

    private fun validateOrder(
        order: Order?,
        paymentTypeEnum: PaymentTypeEnum,
    ): Order =
        order
            .orThrow { businessExceptionByPaymentType(paymentTypeEnum) }
            .also { throwIfNotAllowedToPay(orderStatus = it.status, paymentTypeEnum) }

    private fun resolveCurrentBank(bank: BankEnum?): BankEnum = bankDao.resolveBank(bank)

    private fun registerPayment(
        order: Order,
        paymentTypeEnum: PaymentTypeEnum,
        urlToReturn: UrlToReturn,
        headersParams: GpbSbpHeadersParams?,
    ): Payment =
        order
            .also(::logOrderInfoBeforeRegistrationPayment)
            .run { registerPaymentService.register(order, paymentTypeEnum, urlToReturn, headersParams) }
            .also(::logRegisteredPaymentInfo)

    private fun renewOrder(payment: Payment) =
        payment.order!!
            .apply { updateDate = LocalDateTime.now() }
            .run(orderDao::save)

    private fun businessExceptionByPaymentType(paymentTypeEnum: PaymentTypeEnum) =
        when (paymentTypeEnum) {
            PaymentTypeEnum.CARD -> BusinessException(CODE_ERROR_ORDER_NOT_FOUND)
            PaymentTypeEnum.SBP -> BusinessException(CODE_ERROR_ORDER_NOT_FOUND_SBP)
        }

    private fun throwIfNotAllowedToPay(
        orderStatus: OrderStatus,
        paymentTypeEnum: PaymentTypeEnum,
    ) = resolveErrorCodeIfNotAllowedToPay(orderStatus, paymentTypeEnum)
        ?.let { throw BusinessException(it) }

    private fun resolveErrorCodeIfNotAllowedToPay(
        orderStatus: OrderStatus,
        paymentTypeEnum: PaymentTypeEnum,
    ): Int? =
        when {
            orderStatus.isPaidFor() && paymentTypeEnum == PaymentTypeEnum.CARD -> CODE_ERROR_ORDER_IS_PAID_FOR
            orderStatus.isPaidFor() && paymentTypeEnum == PaymentTypeEnum.SBP -> CODE_ERROR_ORDER_IS_PAID_FOR_SBP
            orderStatus.isNotAvailable() && paymentTypeEnum == PaymentTypeEnum.CARD -> CODE_ERROR_ORDER_IS_NOT_AVAILABLE
            orderStatus.isNotAvailable() && paymentTypeEnum == PaymentTypeEnum.SBP -> CODE_ERROR_ORDER_IS_NOT_AVAILABLE_SBP
            else -> null
        }

    private fun logOrderInfoBeforeRegistrationPayment(order: Order) =
        LOG_ORDER_INFO_BEFORE_REGISTRATION
            .format(order.id, order.bank)
            .run(logger::info)

    private fun logRegisteredPaymentInfo(payment: Payment) =
        LOG_PAYMENT_INFO_AFTER_REGISTRATION
            .format(payment.order?.id, payment.bank)
            .run(logger::info)

    @Throws(BusinessException::class)
    private fun Payment.toDataPay(): DataPay {
        val url = paymentPageUrl ?: throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE)
        return DataPay(url)
    }

    private fun Payment.toResponseStatusPay() =
        ResponseStatusPay(
            paymentStatus = state,
            cheque = chequeName == ChequeStateEnum.SENT.name,
        )

    private fun <T : Any> T.wrapToSuccessResponse(status: Int) = getSuccessResponse(getTraceId(), status, this)
}
