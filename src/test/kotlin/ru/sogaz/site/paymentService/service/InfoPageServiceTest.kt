package ru.sogaz.site.paymentService.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.data.FileQR
import ru.sogaz.site.paymentService.dto.data.PaySbp
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.MediaTypeValue
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.payment.InfoPageServiceImpl
import ru.sogaz.site.paymentService.service.payment.PaymentServiceImpl
import java.util.UUID

@ExtendWith(MockKExtension::class)
class InfoPageServiceTest {
    companion object {
        private const val QR_CONTENT = "QR Content"

        private val validFileQR = FileQR(QR_CONTENT, MediaTypeValue.IMAGE_PNG_VALUE)
        private val validPaySbp = PaySbp("", validFileQR)
    }

    private lateinit var infoPageService: InfoPageService

    @MockK
    private lateinit var orderDao: OrderDao

    @MockK
    private lateinit var configDataDao: ConfigDataDao

    @MockK
    private lateinit var registerPaymentService: RegisterPaymentService

    @MockK
    private lateinit var qrCodeService: QRCodeService

    @RelaxedMockK
    private lateinit var apiConfigProperties: ApiConfigProperties

    private lateinit var validOrderUUID: UUID
    private lateinit var inValidOrderUUID: UUID
    private lateinit var validOrder: Order

    @BeforeEach
    fun beforeEach() {
        validOrderUUID = UUID.randomUUID()
        inValidOrderUUID = UUID.randomUUID()
        validOrder = Order(id = validOrderUUID)

        infoPageService = initInfoPageService()

        every { orderDao.findById(validOrderUUID) } returns validOrder
        every { orderDao.findById(inValidOrderUUID) } throws InnerException(OrderServiceTest.TRACE_ID, "DB error")
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo with qr get from qr-generator-service`() {
        setSbpActiveConfigValue(true)
        every { qrCodeService.generatePaySbp(any(String::class)) } returns validPaySbp

        infoPageService
            .getInfo(validOrderUUID)
            .run(::assertThat)
            .returns(validOrderUUID.toString()) { it.orderId }
            .returns(validFileQR) { it.paySbp?.fileQR }

        verify { qrCodeService.generatePaySbp(any(String::class)) }
        verify(inverse = true) { qrCodeService.requestFromBank(any(Payment::class)) }
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo with qr get from gpb-bank`() {
        setSbpActiveConfigValue(true)
        every { qrCodeService.generatePaySbp(any(String::class)) } returns null
        every { registerPaymentService.register(any(), PaymentTypeEnum.SBP, any()) }.returns(Payment(qrcId = "qr-id"))
        every { qrCodeService.requestFromBank(any(Payment::class)) } returns validPaySbp

        infoPageService
            .getInfo(validOrderUUID)
            .run(::assertThat)
            .isNotNull
            .returns(validOrderUUID.toString()) { it.orderId }
            .returns(validFileQR) { it.paySbp?.fileQR }

        verify { qrCodeService.generatePaySbp(any(String::class)) }
        verify { qrCodeService.requestFromBank(any(Payment::class)) }
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo without sbp pay info`() {
        setSbpActiveConfigValue(false)

        infoPageService
            .getInfo(validOrderUUID)
            .run(::assertThat)
            .returns(validOrderUUID.toString()) { it.orderId }
            .returns(null) { it.paySbp?.urlPay }
            .returns(null) { it.paySbp?.fileQR }

        verify(inverse = true) { qrCodeService.generatePaySbp(any(String::class)) }
        verify(inverse = true) { qrCodeService.requestFromBank(any(Payment::class)) }
    }

    private fun setSbpActiveConfigValue(value: Boolean) =
        every { configDataDao.findByKey(PaymentServiceImpl.SBP_ACTIVE_CONFIG_NAME, Boolean::class) }
            .returns(value)

    private fun initInfoPageService() =
        InfoPageServiceImpl(
            orderDao = orderDao,
            configDataDao = configDataDao,
            registerPaymentService = registerPaymentService,
            qrCodeService = qrCodeService,
            apiConfigProperties = apiConfigProperties,
        )
}
