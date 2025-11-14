package ru.sogaz.site.paymentService.service.payment

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_CANNOT_BE_PAID_INFO
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND_INFO
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.findByKey
import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.data.PaySbp
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.order.OrderMapper
import ru.sogaz.site.paymentService.orThrow
import ru.sogaz.site.paymentService.service.InfoPageService
import ru.sogaz.site.paymentService.service.QRCodeService
import ru.sogaz.site.paymentService.service.RegisterPaymentService
import java.net.URI
import java.util.Objects.isNull
import java.util.UUID

@Service
class InfoPageServiceImpl(
    private val orderDao: OrderDao,
    private val configDataDao: ConfigDataDao,
    private val registerPaymentService: RegisterPaymentService,
    private val qrCodeService: QRCodeService,
    private val orderMapper: OrderMapper,
) : InfoPageService {
    companion object {
        private const val SBP_ACTIVE_CONFIG_NAME = "sbpActive"
        private const val LOG_INFO_PAGE_WITHOUT_SBP_QR = "Для заказа с id: %s не будет отображена оплата по QR коду с СБП"
        private const val LOG_ERROR_TO_GET_QR_FROM_BANK = "Не удалось получить QR код из банка %s, ex: %s"
        private const val LOG_FULL_INFO_PAGE =
            "Для генерации информации по платежу для заказа с id: %s будет отображена оплата по QR коду с СБП"
    }

    @Value("\${api.payment.paymentUrl}")
    lateinit var cardPayBaseUri: String

    @Value("\${api.payment.sbpPaymentUrl}")
    lateinit var sbpPayBaseUri: String

    private val logger = loggerFor(javaClass)
    private val objectMapper: ObjectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)

    override fun getInfo(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo =
        orderId
            .run(orderDao::findById)
            .orThrow { BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO) }
            .also(::checkOrderStatus)
            .run { getInfo(this, payQueryParams) }

    override fun getInfo(
        order: Order,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo =
        order
            .run { formPaymentPageInfo(this, payQueryParams) }
            .also(::logPageInfoResult)

    private fun checkOrderStatus(order: Order) {
        if (order.status.isPaidFor() || order.status.isNotAvailable()) {
            throw BusinessException(CODE_ERROR_ORDER_CANNOT_BE_PAID_INFO)
        }
    }

    private fun formPaymentPageInfo(
        order: Order,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo =
        order
            .run { formPaySbpInfo(this, payQueryParams) }
            .run { formPaymentPageInfo(this, order, payQueryParams) }

    private fun formPaymentPageInfo(
        paySbp: PaySbp?,
        order: Order,
        payQueryParams: PayQueryParams,
    ): DataOrderPaymentPageInfo =
        orderMapper.toOrderPaymentPageInfo(
            order = order,
            paySbp = paySbp,
            urlPayBank = buildCardPayUri(order.id!!, payQueryParams),
        )

    private fun formPaySbpInfo(
        order: Order,
        payQueryParams: PayQueryParams,
    ): PaySbp? {
        if (isSBPActive().not()) {
            return null
        }
        val paySbpLink = buildSbpPayUri(order.id!!, payQueryParams)
        return formPaySbpInfo(order, paySbpLink)
    }

    private fun formPaySbpInfo(
        order: Order,
        spbPayUrl: URI,
    ): PaySbp? =
        generatePaySbpFromQRGeneratorService(spbPayUrl)
            ?: generatePaySbpFromBank(order)

    private fun generatePaySbpFromQRGeneratorService(spbPayUrl: URI): PaySbp? =
        spbPayUrl
            .run(qrCodeService::generateFileQR)
            ?.let { PaySbp(spbPayUrl, it) }

    private fun generatePaySbpFromBank(order: Order): PaySbp? {
        if (order.bank == BankEnum.AKB_RUS) {
            return null
        }
        return try {
            order
                .run(::registerSbpPayment)
                .run(::generatePaySbpFromBank)
        } catch (ex: Exception) {
            LOG_ERROR_TO_GET_QR_FROM_BANK
                .format(order.bank, ex.message)
                .run(logger::debug)
            null
        }
    }

    private fun registerSbpPayment(order: Order): Payment = registerPaymentService.register(order, PaymentTypeEnum.SBP)

    private fun generatePaySbpFromBank(payment: Payment): PaySbp? =
        payment
            .run(qrCodeService::requestFileQRFromBank)
            ?.let { PaySbp(payment.paymentPageUrl.toString(), it) }

    private fun buildSbpPayUri(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): URI =
        buildUri(
            "$sbpPayBaseUri$orderId",
            payQueryParams.toQueryParams(),
        )

    private fun buildCardPayUri(
        orderId: UUID,
        payQueryParams: PayQueryParams,
    ): URI =
        buildUri(
            "$cardPayBaseUri$orderId",
            payQueryParams.toQueryParams(),
        )

    private fun buildUri(
        uriString: String,
        params: MultiValueMap<String, String>,
    ): URI =
        UriComponentsBuilder
            .fromUriString(uriString)
            .queryParams(params)
            .toUriString()
            .run(URI::create)

    private fun PayQueryParams.toQueryParams(): MultiValueMap<String, String> = MultiValueMap.fromSingleValue(toMap())

    private fun PayQueryParams.toMap(): Map<String, String> = objectMapper.convertValue(this)

    private fun logPageInfoResult(pageInfo: DataOrderPaymentPageInfo) = getLogMessageForPageInfo(pageInfo).run(logger::debug)

    private fun getLogMessageForPageInfo(pageInfo: DataOrderPaymentPageInfo) =
        if (isNull(pageInfo.paySbp)) {
            LOG_INFO_PAGE_WITHOUT_SBP_QR.format(pageInfo.orderId)
        } else {
            LOG_FULL_INFO_PAGE.format(pageInfo.orderId)
        }

    private fun isSBPActive(): Boolean = configDataDao.findByKey(SBP_ACTIVE_CONFIG_NAME)
}
