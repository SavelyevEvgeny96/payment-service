package ru.sogaz.site.paymentService.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.spy
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.ConfigDataDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dto.data.FileQR
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.MediaTypeValue
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.mapper.OrderMapper
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.payment.PaymentServiceImpl
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class PaymentServiceTest {
    companion object {
        const val TRACE_ID = "trace-123"
        const val PAY_CARD_GPB_URL = "pay-card-url"
        const val PAY_SBP_GPB_URL = "pay-sbp-gpb-url"
        private const val QR_CONTENT = "QR Content"
        private const val SUCCESS_UPDATE_PAYMENT_INVOICE_MESSAGE = "ok"

        private val VALID_FILE_QR = FileQR(QR_CONTENT, MediaTypeValue.IMAGE_PNG_VALUE)
    }

    @MockK
    private lateinit var orderDao: OrderDao

    @MockK
    private lateinit var bankDao: BankDao

    @MockK
    private lateinit var configDataDao: ConfigDataDao

    @MockK
    private lateinit var registerPaymentService: RegisterPaymentService

    @MockK
    private lateinit var qrCodeService: QRCodeService

    @MockK
    private lateinit var subOrderService: SubOrderService

    @MockK
    private lateinit var orderMapper: OrderMapper

    @RelaxedMockK
    private lateinit var waitingPaymentDao: WaitingPaymentDao

    @RelaxedMockK
    private lateinit var apiConfigProperties: ApiConfigProperties

    private lateinit var paymentService: PaymentService

    private lateinit var validOrderUUID: UUID
    private lateinit var inValidOrderUUID: UUID
    private lateinit var validOrder: Order

    @BeforeEach
    fun beforeEach() {
        validOrderUUID = UUID.randomUUID()
        inValidOrderUUID = UUID.randomUUID()
        validOrder = Order(id = validOrderUUID)

        every { orderDao.findById(validOrderUUID) } returns validOrder
        every { orderDao.findById(inValidOrderUUID) } throws InnerException(OrderServiceTest.TRACE_ID, "DB error")
        every { bankDao.resolveBank(any<BankEnum>()) } returnsArgument 0
        every { orderDao.save(any<Order>()) } returnsArgument 0

        paymentService = spy(initPaymentService())
    }

    @Test
    fun `createPayment should update order and add payment in queue`() {
        validOrder.apply {
            bank = BankEnum.GPB
            status = OrderStatus.NEW
        }
        val registeredPayment = buildGPBRegisteredSBPPayment()

        every { registerPaymentService.register(validOrder, PaymentTypeEnum.CARD, any()) } returns registeredPayment

        paymentService
            .createCardPayment(validOrderUUID)
            .run(::assertThat)
            .returns(PAY_CARD_GPB_URL) { it.data?.paymentPageUrl }
        verify { orderDao.save(validOrder) }
        verify { waitingPaymentDao.saveWaitingForPayment(registeredPayment) }
    }

    @Test
    fun `createPayment shouldn't update order and add payment in queue if error occurs`() {
        validOrder.apply {
            bank = BankEnum.GPB
            status = OrderStatus.NEW
        }
        val registeredPayment = buildGPBRegisteredSBPPayment()

        every { registerPaymentService.register(validOrder, PaymentTypeEnum.CARD, any()) }
            .throws(BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_AVAILABLE))

        assertThrows<BusinessException> { paymentService.createCardPayment(validOrderUUID) }
        verify(inverse = true) { orderDao.save(validOrder) }
        verify(inverse = true) { waitingPaymentDao.saveWaitingForPayment(registeredPayment) }
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo with qr get from qr-generator-service`() {
        setSbpActiveConfigValue(true)
        every { qrCodeService.generateQRCode(any(String::class)) }.returns(Optional.of(VALID_FILE_QR))

        paymentService
            .getOrderPaymentPageInfo(validOrderUUID)
            .run(::assertThat)
            .returns(validOrderUUID.toString()) { it.data!!.orderId }
            .returns(VALID_FILE_QR) { it.data!!.paySbp?.fileQR }

        verify { qrCodeService.generateQRCode(any(String::class)) }
        verify(inverse = true) { qrCodeService.requestFromBank(any(Payment::class)) }
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo with qr get from gpb-bank`() {
        setSbpActiveConfigValue(true)
        every { qrCodeService.generateQRCode(any(String::class)) }.returns(Optional.empty())
        every { registerPaymentService.register(any(), PaymentTypeEnum.SBP, any()) }.returns(Payment(qrcId = "qr-id"))
        every { qrCodeService.requestFromBank(any(Payment::class)) }.returns(Optional.of(VALID_FILE_QR))

        paymentService
            .getOrderPaymentPageInfo(validOrderUUID)
            .run(::assertThat)
            .isNotNull
            .returns(validOrderUUID.toString()) { it.data?.orderId }
            .returns(VALID_FILE_QR) { it.data!!.paySbp?.fileQR }

        verify { qrCodeService.generateQRCode(any(String::class)) }
        verify { qrCodeService.requestFromBank(any(Payment::class)) }
    }

    @Test
    fun `getOrderPaymentPageInfo should return valid dataOrderPaymentPageInfo without sbp pay info`() {
        setSbpActiveConfigValue(false)

        paymentService
            .getOrderPaymentPageInfo(validOrderUUID)
            .run(::assertThat)
            .returns(validOrderUUID.toString()) { it.data!!.orderId }
            .returns(null) { it.data?.paySbp?.urlPay }
            .returns(null) { it.data!!.paySbp?.fileQR }

        verify(inverse = true) { qrCodeService.generateQRCode(any(String::class)) }
        verify(inverse = true) { qrCodeService.requestFromBank(any(Payment::class)) }
    }

    @Test
    fun `updatePaymentInvoice should return success response`() {
        every { subOrderService.updateSubOrder(buildUpdatePaymentInvoiceRequest()) } returns SubOrder()
        every { orderMapper.updateOrder(buildUpdatePaymentInvoiceRequest(), validOrder) } returns validOrder

        val result = paymentService.updatePaymentInvoice(buildUpdatePaymentInvoiceRequest())

        assertEquals(SUCCESS_UPDATE_PAYMENT_INVOICE_MESSAGE, result.data?.status)
    }

    private fun setSbpActiveConfigValue(value: Boolean) =
        every { configDataDao.getBankInfoFromConfigData(any(), PaymentServiceImpl.SBP_ACTIVE_CONFIG_NAME) }
            .returns(value.toString())

    private fun initPaymentService() =
        PaymentServiceImpl(
            orderDao,
            bankDao,
            configDataDao,
            registerPaymentService,
            qrCodeService,
            apiConfigProperties,
            subOrderService,
            orderMapper,
            waitingPaymentDao,
        )

    private fun buildUpdatePaymentInvoiceRequest(): UpdatePaymentInvoiceRequest =
        UpdatePaymentInvoiceRequest(
            validOrderUUID,
            null,
            BigDecimal.TEN,
            "testemail@gmail.com",
            null,
            true,
        )

    private fun buildGPBRegisteredSBPPayment() = buildRegisteredPayment(PAY_CARD_GPB_URL, PaymentTypeEnum.SBP)

    private fun buildGPBRegisteredCARDPayment() = buildRegisteredPayment(PAY_SBP_GPB_URL, PaymentTypeEnum.CARD)

    private fun buildRegisteredPayment(
        paymentPageUrl: String,
        type: PaymentTypeEnum,
    ): Payment =
        Payment(
            order = validOrder,
            paymentPageUrl = paymentPageUrl,
            type = type,
        )
}
