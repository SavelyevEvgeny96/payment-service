package ru.sogaz.site.paymentService.service.payment

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_NOT_AVAILABLE_SBP
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_IS_PAID_FOR_SBP
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND_INFO
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND_SBP
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.data.FileQR
import ru.sogaz.site.paymentService.dto.data.PaySbp
import ru.sogaz.site.paymentService.dto.data.UrlToReturn
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.QRCodeService
import ru.sogaz.site.paymentService.service.RegisterPaymentService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.util.Objects.isNull
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

/**
 * Сервис для обработки платежей.
 * Включает в себя валидацию данных и создание записи о платеже.
 */
class PaymentServiceImpl(
    private val orderDao: OrderDao,
    private val bankDao: BankDao,
    private val configDataDao: ConfigDataDao,
    private val registerPaymentService: RegisterPaymentService,
    private val qrCodeService: QRCodeService,
    private val apiConfigProperties: ApiConfigProperties,
) : PaymentService {
    companion object {
        const val SUCCESS_STATUS_CODE_CARD_PAY = 1101510200
        const val SUCCESS_STATUS_CODE_SBP_PAY = 1101530200
        const val SUCCESS_STATUS_CODE_PAY_INFO_PAGE = 1101540200

        const val SBP_ACTIVE_CONFIG_NAME = "sbpActive"
        const val LOG_ERROR_PAYMENT_TYPE_IS_ABSENT = "Отсутствует тип платежа"
        const val LOG_ORDER_INFO_BEFORE_REGISTRATION = "Для регистрации платежа по заказу с id: %s был выбран банк: %s"
        const val LOG_PAYMENT_INFO_AFTER_REGISTRATION = "Платеж по заказу с id: %s был успешно зарегистрирован в банке: %s"
        const val LOG_INFO_PAGE_WITHOUT_SBP_QR =
            "Для заказа с id: %s не будет отображена оплата по QR коду с СБП"
        const val LOG_FULL_INFO_PAGE =
            "Для генерации информации по платежу для заказа с id: %s будет отображена оплата по QR коду с СБП"
        const val LOG_ERROR_TO_GET_QR_FROM_BANK = "Не удалось получить QR код из банка %s, ex: %s"
    }

    private val logger = loggerFor(javaClass)

    override fun createCardPayment(
        orderId: UUID,
        urlToReturnS: String?,
        urlToReturnF: String?,
    ): Response<DataPay> =
        createPayment(
            orderId,
            PaymentTypeEnum.CARD,
            UrlToReturn(urlToReturnS, urlToReturnF),
        )

    override fun createSBPPayment(
        orderId: UUID,
        urlToReturnS: String?,
        urlToReturnF: String?,
    ): Response<DataPay> =
        createPayment(
            orderId,
            PaymentTypeEnum.SBP,
            UrlToReturn(urlToReturnS, urlToReturnF),
        )

    private fun createPayment(
        orderId: UUID,
        paymentTypeEnum: PaymentTypeEnum,
        urlToReturn: UrlToReturn,
    ): Response<DataPay> =
        orderDao
            .findById(orderId)
            .orElseThrow { businessExceptionByPaymentType(paymentTypeEnum) }
            .also { throwIfNotAllowedToPay(orderStatus = it.status, paymentTypeEnum) }
            .apply { bank = resolveCurrentBank(bank) }
            .also(::logOrderInfoBeforeRegistrationPayment)
            .run { registerPayment(this, paymentTypeEnum, urlToReturn) }
            .also(::logRegisteredPaymentInfo)
            .run(::getSuccessResponse)

    private fun resolveCurrentBank(bank: BankEnum?): BankEnum = bankDao.resolveBank(bank)

    private fun registerPayment(
        order: Order,
        paymentTypeEnum: PaymentTypeEnum,
        urlToReturn: UrlToReturn,
    ): Payment = registerPaymentService.register(order, paymentTypeEnum, urlToReturn)

    override fun getOrderPaymentPageInfo(orderId: UUID): Response<DataOrderPaymentPageInfo> =
        findOrderForQROrThrow(orderId)
            .also(::checkOrderStatus)
            .run(::formPaySBPInfo)
            .run { formOrderPaymentPageInfo(orderId, this) }
            .also(::logPageInfoResultInfo)
            .run { getSuccessResponse(getTraceId(), SUCCESS_STATUS_CODE_PAY_INFO_PAGE, this) }

    private fun findOrderForQROrThrow(orderId: UUID) =
        orderDao
            .findById(orderId)
            .orElseThrow { createBusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO) }

    private fun checkOrderStatus(order: Order) {
        if (order.status.isPaidFor() || order.status.isNotAvailable()) {
            throw BusinessException(CODE_ERROR_ORDER_IS_PAID_FOR)
        }
    }

    private fun formPaySBPInfo(order: Order): PaySbp? {
        if (isSBPActive().not()) {
            return null
        }

        return formSPBPayLink(order.id.toString())
            .run { formPaySBPInfo(order, this) }
    }

    private fun isSBPActive() = configDataDao.getBankInfoFromConfigData(getTraceId(), SBP_ACTIVE_CONFIG_NAME).toBoolean()

    private fun formPaySBPInfo(
        order: Order,
        spbPayUrl: String,
    ): PaySbp? =
        qrCodeService
            .generateQRCode(spbPayUrl)
            .orElseGet { getQRCodeFromBank(order) }
            ?.let { PaySbp(urlPay = spbPayUrl, fileQR = it) }

    private fun getQRCodeFromBank(order: Order): FileQR? =
        try {
            order
                .apply { bank = BankEnum.GPB }
                .run { registerPaymentService.register(this, PaymentTypeEnum.SBP) }
                .run { qrCodeService.requestFromBank(this) }
                .getOrNull()
        } catch (ex: Exception) {
            LOG_ERROR_TO_GET_QR_FROM_BANK
                .format(BankEnum.GPB, ex.message)
                .run(logger::info)
            null
        }

    private fun formOrderPaymentPageInfo(
        orderId: UUID,
        paySbp: PaySbp?,
    ): DataOrderPaymentPageInfo =
        DataOrderPaymentPageInfo(
            orderId = orderId.toString(),
            urlPayBank = formCardPayLink(orderId.toString()),
            paySbp = paySbp,
        )

    private fun formCardPayLink(orderId: String): String = "${apiConfigProperties.qrUrlForCardPayment}$orderId"

    private fun formSPBPayLink(orderId: String): String = "${apiConfigProperties.qrUrlForSbpPayment}$orderId"

    private fun businessExceptionByPaymentType(paymentTypeEnum: PaymentTypeEnum) =
        when (paymentTypeEnum) {
            PaymentTypeEnum.CARD -> createBusinessException(CODE_ERROR_ORDER_NOT_FOUND)
            PaymentTypeEnum.SBP -> createBusinessException(CODE_ERROR_ORDER_NOT_FOUND_SBP)
        }

    private fun createBusinessException(code: Int) = BusinessException(code, getTraceId())

    private fun throwIfNotAllowedToPay(
        orderStatus: OrderStatus,
        paymentTypeEnum: PaymentTypeEnum,
    ) = resolveErrorCodeIfNotAllowedToPay(orderStatus, paymentTypeEnum)
        ?.let { throw BusinessException(it, getTraceId()) }

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

    private fun getSuccessResponse(payment: Payment) =
        when (payment.type) {
            PaymentTypeEnum.CARD -> getSuccessResponse(payment, SUCCESS_STATUS_CODE_SBP_PAY)
            PaymentTypeEnum.SBP -> getSuccessResponse(payment, SUCCESS_STATUS_CODE_CARD_PAY)
            else -> throw InnerException(getTraceId(), LOG_ERROR_PAYMENT_TYPE_IS_ABSENT)
        }

    private fun getSuccessResponse(
        payment: Payment,
        code: Int,
    ) = getSuccessResponse(getTraceId(), code, DataPay(payment.paymentPageUrl))

    private fun logOrderInfoBeforeRegistrationPayment(order: Order) =
        LOG_ORDER_INFO_BEFORE_REGISTRATION
            .format(order.id, order.bank)
            .run(logger::info)

    private fun logRegisteredPaymentInfo(payment: Payment) =
        LOG_PAYMENT_INFO_AFTER_REGISTRATION
            .format(payment.order?.id, payment.bank)
            .run(logger::info)

    private fun logPageInfoResultInfo(pageInfo: DataOrderPaymentPageInfo) = getLogMessageForPageInfo(pageInfo).run(logger::info)

    private fun getLogMessageForPageInfo(pageInfo: DataOrderPaymentPageInfo) =
        if (isNull(pageInfo.paySbp)) {
            LOG_INFO_PAGE_WITHOUT_SBP_QR.format(pageInfo.orderId)
        } else {
            LOG_FULL_INFO_PAGE.format(pageInfo.orderId)
        }
}
