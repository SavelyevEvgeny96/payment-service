package service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.PaymentAkbStatusResponse
import ru.sogaz.site.paymentService.dto.data.PaidOrderMessage
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentStatus
import ru.sogaz.site.paymentService.entity.PaymentType
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.PrevStatusEnum
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.HistoryService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.PaymentStatusCheckerServiceImpl
import java.time.LocalDateTime

class PaymentStatusCheckerServiceTest {
    private val receiptService = mock<ReceiptService>()
    private val restTemplate = mock<WebConfigRestTemplate>()
    private val restTemplateMock = mock<RestTemplate>()
    private val apiConfigProperty = mock<ApiConfigProperties>()
    private val rabbitTemplate = mock<RabbitTemplate>()
    private val objectMapper = mock<ObjectMapper>()
    private val rabbitProperties = mock<RabbitProperties>()
    private val subOrderDao = mock<SubOrderDao>()
    private val paymentDao = mock<PaymentDao>()
    private val paymentStatusDao = mock<PaymentStatusDao>()
    private val orderStatusDao = mock<OrderStatusDao>()
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
            paymentStatusDao = paymentStatusDao,
            orderStatusDao = orderStatusDao,
            operationHistoryDao = operationHistoryDao,
            orderDao = orderDao,
            historyService = historyService,
        )

    private val traceId = getTraceId().apply { "test-trace-id" }

    @Test
    fun testGetStatusWithSuccessfulPayment() {
        val paymentBankId = "test-payment-id"

        val expectedStatus = "SUCCESS"

        val order = Order()
        order.id = 1
        val payment =
            Payment(
                paymentBankId = paymentBankId,
                stateId = PaymentStatus(stateId = expectedStatus, stateName = "Success"),
                id = 1L,
                orderId = order,
            )

        `when`(paymentDao.findByPaymentBankId(paymentBankId)).thenReturn(payment)

        val response = service.getStatus(paymentBankId)

        assertEquals(expectedStatus, response.data?.paymentStatus)
    }

    @Test
    fun `getStatus should call receipt service when cheque needed`() {
        val paymentStatus = PaymentStatus()
        paymentStatus.stateId = "SUCCESS"

        val paymentBankId = "test123"
        val order =
            Order().apply {
                orderId = "TEST-ORDER"
                recipientEmail = "test@example.com"
            }
        val payment =
            Payment().apply {
                this.paymentBankId = paymentBankId
                stateId = paymentStatus
                this.orderId = order
                this.id = 1L
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
                stateId = PaymentStatus(stateId = "REG", stateName = "Registered"),
                id = 1L,
                orderId = Order().apply { id = 1 },
            )
        val status = PaymentStatus().apply { stateId = "REG" }

        `when`(paymentDao.findByPaymentBankId(paymentBankId)).thenReturn(payment)
        `when`(paymentStatusDao.getPaymentStatus(traceId, "REG")).thenReturn(status)

        val response = service.getStatus(paymentBankId)

        assertEquals("REG", response.data?.paymentStatus)
    }

    @Test
    fun `getStatus should throw exception for unknown payment state`() {
        val paymentBankId = "test-payment-id"
        val payment =
            Payment(
                paymentBankId = paymentBankId,
                stateId = PaymentStatus(stateId = "UNKNOWN", stateName = "Unknown"),
                id = 1L,
            )

        `when`(paymentDao.findByPaymentBankId(paymentBankId)).thenReturn(payment)

        assertThrows<BusinessException> {
            service.getStatus(paymentBankId)
        }
    }

    @Test
    fun `processPaymentStatusCheck should process AKB payment for akb_rus bank`() {
        val order =
            Order().apply {
                id = 1
                orderId = "123"
                updateDate = LocalDateTime.now()
                recipientEmail = "test@test.ru"
            }
        val stateId =
            PaymentStatus(
                stateId = "REG",
            )
        val payment =
            Payment(
                id = 1L,
                typeId = PaymentType().apply { typeId = "bankCard" },
                bank = Bank().apply { bankId = "akb_rus" },
                paymentBankId = "test-payment",
                stateId = stateId,
                orderId = order,
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
                id = 1,
                subOrderId = "",
                orderId = order,
                clientSystem =
                    ClientSystem(
                        id = 1,
                        externalSystemCode = "externalSystemCode",
                        externalSystemName = "externalSystemName",
                    ),
                docType = "docType",
                policyId = "policyId",
                policyNumber = "policyNumber",
                contractNumber = "contractNumber",
                contractId = "contractId",
                typeInsurance = "typeInsurance",
                premiumAmount = "100.0",
                updateDate = LocalDateTime.now(),
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
        `when`(order.orderId?.let { orderDao.getOrderId(it) }).thenReturn(order)
        `when`(paymentDao.findByPaymentBankId(paymentBankId)).thenReturn(payment)
        `when`(paymentStatusDao.getPaymentStatus(traceId, "SUCCESS")).thenReturn(PaymentStatus(stateId = "SUCCESS"))
        `when`(orderStatusDao.getOrderStatus(traceId, "SUCCESS")).thenReturn(OrderStatus(stateId = "SUCCESS"))
        `when`(subOrderDao.getAllSubOrderListByOrderId(org.mockito.kotlin.eq(order), org.mockito.kotlin.any())).thenReturn(listOf(subOrder))
        val mockPayment =
            Payment(
                id = 1,
                stateId = PaymentStatus(1, "SUCCESS"),
            )
        `when`(paymentDao.save(eq(mockPayment))).thenReturn(1L)
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
                PaymentAkbStatusResponse::class.java
            )
        assertEquals("Status", paymentAkbStatusResponse.status)
        assertEquals(PrevStatusEnum.PREPARING, paymentAkbStatusResponse.prevStatus)
    }
}
