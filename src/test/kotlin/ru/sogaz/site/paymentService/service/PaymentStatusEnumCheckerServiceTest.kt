package ru.sogaz.site.paymentService.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.any
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.data.PaidOrderMessage
import ru.sogaz.site.paymentService.dto.response.PaymentAkbStatusResponse
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentTypeEnum
import ru.sogaz.site.paymentService.enums.PrevStatusEnum
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.payment.PaymentStatusCheckerServiceImpl
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

class PaymentStatusEnumCheckerServiceTest {
    private val receiptService = mock<ReceiptService>()
    private val restTemplate = mock<WebConfigRestTemplate>()
    private val restTemplateMock = mock<RestTemplate>()
    private val apiConfigProperty = mock<ApiConfigProperties>()
    private val rabbitTemplate = mock<RabbitTemplate>()
    private val objectMapper = mock<ObjectMapper>()
    private val rabbitProperties = mock<RabbitProperties>()
    private val subOrderDao = mock<SubOrderDao>()
    private val paymentDao = mock<PaymentDao>()
    private val operationHistoryDao = mock<PaymentOperationHistoryDao>()
    private val orderDao = mock<OrderDao>()
    private val historyService = mock<HistoryService>()
    private val service =
        PaymentStatusCheckerServiceImpl(
            apiConfigProperty = apiConfigProperty,
            rabbitTemplate = rabbitTemplate,
            receiptService = receiptService,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            rabbit = rabbitProperties,
            subOrderDao = subOrderDao,
            paymentDao = paymentDao,
            operationHistoryDao = operationHistoryDao,
            orderDao = orderDao,
            historyService = historyService,
        )

    private val traceId = "test-trace-id"

    @Test
    fun testGetStatusWithSuccessfulPayment() {
        val paymentBankId = "test-payment-id"

        val expectedStatus = "SUCCESS"

        val order = Order()
        order.id = UUID.randomUUID()
        val payment =
            Payment(
                paymentBankId = paymentBankId,
                state = PaymentStatusEnum.SUCCESS,
                id = UUID.randomUUID(),
                order = order,
            )

        `when`(paymentDao.findByPaymentBankId(paymentBankId)).thenReturn(payment)

        val response = service.getStatus(paymentBankId)

        assertEquals(expectedStatus, response.data?.paymentStatus)
    }

    @Test
    fun `getStatus should call receipt service when cheque needed`() {
        val paymentBankId = "test123"
        val order =
            Order().apply {
                id = UUID.randomUUID()
                recipientEmail = "test@example.com"
            }
        val payment =
            Payment().apply {
                this.paymentBankId = paymentBankId
                state = PaymentStatusEnum.SUCCESS
                this.order = order
                this.id = UUID.randomUUID()
            }

        `when`(paymentDao.findByPaymentBankId(paymentBankId)).thenReturn(payment)

        doNothing().`when`(receiptService).generateReceipt(order)

        val response = service.getStatus(paymentBankId)

        assertThat(response.data?.cheque).isFalse()
    }

    @Test
    fun `getStatus should return WAIT status for REG payment state`() {
        val paymentBankId = "test-payment-id"
        val payment =
            Payment(
                paymentBankId = paymentBankId,
                state = PaymentStatusEnum.REG,
                id = UUID.randomUUID(),
                order = Order().apply { id = UUID.randomUUID() },
            )

        `when`(paymentDao.findByPaymentBankId(paymentBankId)).thenReturn(payment)

        val response = service.getStatus(paymentBankId)

        assertEquals("REG", response.data?.paymentStatus)
    }

    @Test
    fun `processPaymentStatusCheck should process AKB payment for akb_rus bank`() {
        val order =
            Order().apply {
                id = UUID.randomUUID()
                updateDate = LocalDateTime.now()
                recipientEmail = "test@test.ru"
            }
        val payment =
            Payment(
                id = UUID.randomUUID(),
                type = PaymentTypeEnum.CARD,
                bank = BankEnum.AKB_RUS,
                paymentBankId = "test-payment",
                state = PaymentStatusEnum.REG,
                order = order,
            )
        val paymentBankId = "test-payment"
        val akbResponse =
            """
            {
                "status": "Closed",
                "prevStatus": "FullyPaid"
            }
            """.trimIndent()
        val subOrder =
            SubOrder(
                id = UUID.randomUUID(),
                order = order,
                docType = "docType",
                policyId = "policyId",
                policyNumber = "policyNumber",
                contractNumber = "contractNumber",
                contractId = "contractId",
                typeInsurance = "typeInsurance",
                premiumAmount = "100.0",
                updateDate = Instant.now(),
            )
        `when`(restTemplate.defaultRestTemplate()).thenReturn(restTemplateMock)

        `when`(
            restTemplateMock.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                eq(String::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(akbResponse))

        `when`(objectMapper.readValue(akbResponse, PaymentAkbStatusResponse::class.java))
            .thenReturn(PaymentAkbStatusResponse(status = "Closed", prevStatus = PrevStatusEnum.FULLYPAID))
        `when`(order.id?.let { orderDao.findById(it) }).thenReturn(order)
        `when`(paymentDao.findByPaymentBankId(paymentBankId)).thenReturn(payment)
        `when`(subOrderDao.getAllSubOrderListByOrderId(eq(order), org.mockito.kotlin.any())).thenReturn(listOf(subOrder))
        val mockPayment =
            Payment(
                id = UUID.randomUUID(),
                state = PaymentStatusEnum.SUCCESS,
            )
        `when`(paymentDao.save(eq(mockPayment))).thenReturn(mockPayment)
        val expectedJson = "{\"orderId\":\"123\",\"orderAmount\":45.67}"
        `when`(objectMapper.writeValueAsString(any())).thenReturn(expectedJson)
        `when`(rabbitTemplate.convertAndSend(anyString(), anyString(), any(PaidOrderMessage::class.java))).thenAnswer { }
        val response = service.getStatus(paymentBankId)
        assertEquals(1101520200, response.code)
    }

    @Test
    fun `test read json file AkbStatus object`() {
        val objectMapper = ObjectMapper()
        val paymentAkbStatusResponse: PaymentAkbStatusResponse =
            objectMapper.readValue(
                javaClass.classLoader.getResourceAsStream("AkbStatus_response.json"),
                PaymentAkbStatusResponse::class.java,
            )
        assertEquals("Status", paymentAkbStatusResponse.status)
        assertEquals(PrevStatusEnum.PREPARING, paymentAkbStatusResponse.prevStatus)
    }
}
