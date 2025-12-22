package ru.sogaz.site.paymentService.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestClientException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.payment.receipt.client.api.PaymentReceiptControllerApi
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateResponse
import ru.sogaz.site.payment.receipt.client.model.ResponsePaymentReceiptCreateResponse
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.mapper.receipt.ReceiptMapper
import ru.sogaz.site.paymentService.mapper.receipt.ReceiptMapperImpl
import ru.sogaz.site.paymentService.properties.ReceiptProperties
import ru.sogaz.site.paymentService.service.payment.ReceiptServiceImpl
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(ReceiptMapperImpl::class)
class ReceiptServiceTest {
    companion object {
        private const val TEST_RECEIPT_URL = "http://test.url"
        private const val SUCCESS_STATUS = "SUCCESS"
        private const val FAILED_STATUS = "FAILED"
        private const val TEST_CLIENT_EMAIL = "test@example.com"
        private const val TEST_POLICY_NUMBER = "POL123"
        private const val TEST_CONTRACT_NUMBER = "CONT123"
        private const val TEST_AMOUNT = "1000.00"
        private const val TEST_PAYMENT_BANK_ID = "payment-bank-id"
    }

    @MockK
    private lateinit var paymentDao: PaymentDao

    @MockK
    private lateinit var paymentReceiptControllerApi: PaymentReceiptControllerApi

    @MockK
    private lateinit var receiptProperty: ReceiptProperties

    @Autowired
    private lateinit var receiptMapper: ReceiptMapper

    private lateinit var receiptService: ReceiptService

    private lateinit var validOrder: Order
    private lateinit var validSubOrder: SubOrder
    private lateinit var validPayment: Payment

    @BeforeEach
    fun beforeEach() {
        receiptService =
            ReceiptServiceImpl(
                paymentDao = paymentDao,
                receiptMapper = receiptMapper,
                paymentReceiptControllerApi = paymentReceiptControllerApi
            )

        initOrdersTestData()

        every { paymentDao.findByPaymentBankId(any()) } returns validPayment
        every { paymentDao.save(any()) } returnsArgument 0
        every { receiptProperty.receiptUrl } returns TEST_RECEIPT_URL
    }

    @Test
    fun `generateReceipt should create valid request`() {
        val requestSlot = slot<PaymentReceiptCreateRequest>()
        every { paymentReceiptControllerApi.createPaymentCheck(any()) } returns buildSuccessReceiptServiceResponse()

        receiptService.generateReceipt(validPayment)

        verify(exactly = 1) { paymentReceiptControllerApi.createPaymentCheck(capture(requestSlot)) }
        requestSlot.captured
            .run(::assertThat)
            .returns(TEST_CLIENT_EMAIL) { it.client.email }
            .returns(validPayment.depersonalization) { it.depersonalization }
            .returns(TEST_AMOUNT) { it.total.toString() }
    }

    @Test
    fun `generateReceipt should handle API failure response`() {
        every { paymentReceiptControllerApi.createPaymentCheck(any()) } returns buildFailedReceiptServiceResponse()

        assertThrows<InnerException> {
            receiptService.generateReceipt(validPayment)
        }
    }

    @Test
    fun `generateReceipt should handle REST client exception`() {
        every { paymentReceiptControllerApi.createPaymentCheck(any()) } throws RestClientException("API error")

        val exception =
            assertThrows<InnerException> {
                receiptService.generateReceipt(validPayment)
            }

        assertThat(exception.message).contains(ReceiptServiceImpl.ERROR_RECEIPT_GENERATION)
    }

    private fun initOrdersTestData() {
        validSubOrder =
            SubOrder(
                contractNumber = TEST_CONTRACT_NUMBER,
                premiumAmount = TEST_AMOUNT,
            )
        validOrder =
            Order(
                id = UUID.randomUUID(),
                premiumAmount = TEST_AMOUNT,
                recipientEmail = TEST_CLIENT_EMAIL,
            ).apply {
                subOrders.add(validSubOrder)
            }
        validPayment =
            Payment(
                paymentBankId = TEST_PAYMENT_BANK_ID,
                order = validOrder,
            )
    }

    private fun buildSuccessReceiptServiceResponse() = buildReceiptServiceResponse(SUCCESS_STATUS, 200)

    private fun buildFailedReceiptServiceResponse() = buildReceiptServiceResponse(FAILED_STATUS, 500)

    private fun buildReceiptServiceResponse(
        status: String,
        code: Int,
    ) = ResponsePaymentReceiptCreateResponse()
        .status(status)
        .responseUuid(UUID.randomUUID())
        .code(code)
        .traceId("222")
        .data(PaymentReceiptCreateResponse().state("222").externalId("222"))
}
