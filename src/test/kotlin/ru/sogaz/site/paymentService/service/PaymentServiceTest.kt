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
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.WaitingPaymentDao
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.OrderStatus
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.mapper.order.OrderMapper
import ru.sogaz.site.paymentService.service.payment.PaymentServiceImpl
import java.math.BigDecimal
import java.net.URI
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class PaymentServiceTest {
    companion object {
        const val TRACE_ID = "trace-123"
        private const val SUCCESS_UPDATE_PAYMENT_INVOICE_MESSAGE = "ok"
        private val PAY_CARD_GPB_URL: URI = URI.create("http://www.sogaz.ru")
        private val PAY_SBP_GPB_URL: URI = URI.create("http://www.sogaz.ru")
    }

    @MockK
    private lateinit var orderDao: OrderDao

    @MockK
    private lateinit var bankDao: BankDao

    @MockK
    private lateinit var registerPaymentService: RegisterPaymentService

    @MockK
    private lateinit var subOrderService: SubOrderService

    @MockK
    private lateinit var orderMapper: OrderMapper

    @RelaxedMockK
    private lateinit var waitingPaymentDao: WaitingPaymentDao

    @RelaxedMockK
    private lateinit var infoPageService: InfoPageService

    @RelaxedMockK
    private lateinit var paymentStatusService: PaymentStatusService

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
            .returns(PAY_CARD_GPB_URL, DataPay::paymentPageUrl)
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
    fun `updatePaymentInvoice should return success response`() {
        every { subOrderService.updateSubOrder(buildUpdatePaymentInvoiceRequest()) } returns SubOrder()
        every { orderMapper.updateOrder(buildUpdatePaymentInvoiceRequest(), validOrder) } returns validOrder

        val result = paymentService.updatePaymentInvoice(buildUpdatePaymentInvoiceRequest())

        assertEquals(SUCCESS_UPDATE_PAYMENT_INVOICE_MESSAGE, result.data?.status)
    }

    private fun initPaymentService() =
        PaymentServiceImpl(
            orderDao = orderDao,
            bankDao = bankDao,
            registerPaymentService = registerPaymentService,
            subOrderService = subOrderService,
            orderMapper = orderMapper,
            waitingPaymentDao = waitingPaymentDao,
            infoPageService = infoPageService,
            paymentStatusService = paymentStatusService,
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

    private fun buildGPBRegisteredSBPPayment() = buildRegisteredPayment(PAY_CARD_GPB_URL.toString(), PaymentTypeEnum.SBP)

    private fun buildGPBRegisteredCARDPayment() = buildRegisteredPayment(PAY_SBP_GPB_URL.toString(), PaymentTypeEnum.CARD)

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
