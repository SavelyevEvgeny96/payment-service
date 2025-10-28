package ru.sogaz.site.paymentService.service.payment

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_CANNOT_BE_PAID_INFO
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_ORDER_NOT_FOUND_INFO
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.findByKey
import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.data.PaySbp
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.orThrow
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.InfoPageService
import ru.sogaz.site.paymentService.service.QRCodeService
import ru.sogaz.site.paymentService.service.RegisterPaymentService
import java.util.Objects.isNull
import java.util.UUID

@Service
class InfoPageServiceImpl(
    private val orderDao: OrderDao,
    private val configDataDao: ConfigDataDao,
    private val registerPaymentService: RegisterPaymentService,
    private val qrCodeService: QRCodeService,
    private val apiConfigProperties: ApiConfigProperties,
) : InfoPageService {
    companion object {
        const val SBP_ACTIVE_CONFIG_NAME = "sbpActive"
        const val LOG_INFO_PAGE_WITHOUT_SBP_QR = "Для заказа с id: %s не будет отображена оплата по QR коду с СБП"
        const val LOG_ERROR_TO_GET_QR_FROM_BANK = "Не удалось получить QR код из банка %s, ex: %s"
        const val LOG_FULL_INFO_PAGE =
            "Для генерации информации по платежу для заказа с id: %s будет отображена оплата по QR коду с СБП"
    }

    private val logger = loggerFor(javaClass)

    override fun getInfo(orderId: UUID): DataOrderPaymentPageInfo =
        orderDao
            .findById(orderId)
            .orThrow { BusinessException(CODE_ERROR_ORDER_NOT_FOUND_INFO) }
            .also(::checkOrderStatus)
            .run(::getInfo)

    override fun getInfo(order: Order): DataOrderPaymentPageInfo =
        order
            .run(::formPaymentPageInfo)
            .also(::logPageInfoResultInfo)

    private fun checkOrderStatus(order: Order) {
        if (order.status.isPaidFor() || order.status.isNotAvailable()) {
            throw BusinessException(CODE_ERROR_ORDER_CANNOT_BE_PAID_INFO)
        }
    }

    private fun formPaymentPageInfo(order: Order): DataOrderPaymentPageInfo =
        order
            .run(::formPaySBPInfo)
            .run { formOrderPaymentPageInfo(order.id, this) }

    private fun formPaySBPInfo(order: Order): PaySbp? {
        if (isSBPActive().not()) {
            return null
        }
        val paySbpLink = formSPBPayLink(order.id.toString())
        return formPaySBPInfo(order, paySbpLink)
    }

    private fun isSBPActive(): Boolean = configDataDao.findByKey(SBP_ACTIVE_CONFIG_NAME)

    private fun formSPBPayLink(orderId: String): String = "${apiConfigProperties.qrUrlForSbpPayment}$orderId"

    private fun formPaySBPInfo(
        order: Order,
        spbPayUrl: String,
    ): PaySbp? = qrCodeService.generatePaySbp(spbPayUrl) ?: getQRCodeFromBank(order)

    private fun getQRCodeFromBank(order: Order): PaySbp? {
        if (order.bank == BankEnum.AKB_RUS) {
            return null
        }
        try {
            return registerPaymentService
                .register(order, PaymentTypeEnum.SBP)
                .run(qrCodeService::requestFromBank)
        } catch (ex: Exception) {
            LOG_ERROR_TO_GET_QR_FROM_BANK
                .format(order.bank, ex.message)
                .run(logger::info)
        }
        return null
    }

    private fun formOrderPaymentPageInfo(
        orderId: UUID?,
        paySbp: PaySbp?,
    ): DataOrderPaymentPageInfo =
        DataOrderPaymentPageInfo(
            orderId = orderId.toString(),
            urlPayBank = formCardPayLink(orderId.toString()),
            paySbp = paySbp,
        )

    private fun formCardPayLink(orderId: String): String = "${apiConfigProperties.qrUrlForCardPayment}$orderId"

    private fun logPageInfoResultInfo(pageInfo: DataOrderPaymentPageInfo) = getLogMessageForPageInfo(pageInfo).run(logger::info)

    private fun getLogMessageForPageInfo(pageInfo: DataOrderPaymentPageInfo) =
        if (isNull(pageInfo.paySbp)) {
            LOG_INFO_PAGE_WITHOUT_SBP_QR.format(pageInfo.orderId)
        } else {
            LOG_FULL_INFO_PAGE.format(pageInfo.orderId)
        }
}
