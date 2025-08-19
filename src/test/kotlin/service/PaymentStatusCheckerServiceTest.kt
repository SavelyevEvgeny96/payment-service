package service

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.check
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentService.dto.PaymentAkbStatusResponse
import ru.sogaz.site.paymentService.entity.Bank
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentStatus
import ru.sogaz.site.paymentService.entity.PaymentType
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.PaymentStatusCheckerServiceImpl
import java.util.Optional

class PaymentStatusCheckerServiceTest {
    private val paymentRepository = mock<PaymentRepository>()
    private val receiptService = mock<ReceiptService>()
    private val orderRepository = mock<OrderRepository>()
    private val restTemplate = mock<RestTemplate>()
    private val subOrderRepository = mock<SubOrderRepository>()
    private val actionTypeRepository = mock<ActionTypeRepository>()
    private val operationHistoryRepository = mock<PaymentOperationHistoryRepository>()
    private val paymentStatusRepository = mock<PaymentStatusRepository>()
    private val apiConfigProperty = mock<ApiConfigProperties>()
    private val orderStatusRepository = mock<OrderStatusRepository>()
    private val rabbitTemplate = mock<RabbitTemplate>()
    private val objectMapper = mock<ObjectMapper>()
    private val rabbitProperties = mock<RabbitProperties>()

    private val service =
        PaymentStatusCheckerServiceImpl(
            orderRepository = orderRepository,
            subOrderRepository = subOrderRepository,
            actionTypeRepository = actionTypeRepository,
            operationHistoryRepository = operationHistoryRepository,
            paymentStatusRepository = paymentStatusRepository,
            paymentRepository = paymentRepository,
            apiConfigProperty = apiConfigProperty,
            orderStatusRepository = orderStatusRepository,
            rabbitTemplate = rabbitTemplate,
            receiptService = receiptService,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            rabbit = rabbitProperties,
        )

    private val traceId = "test-trace-id"

    @Test
    fun testGetStatusWithSuccessfulPayment() {
        val paymentBankId = "test-payment-id"
        val traceId = "trace-test"
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

        `when`(paymentRepository.findByPaymentBankId(paymentBankId)).thenReturn(payment)
        `when`(paymentRepository.findById(payment.id!!)).thenReturn(Optional.of(payment))

        val response = service.getStatus(paymentBankId, traceId)

        assertEquals(expectedStatus, response.data?.paymentStatus)
    }

    @Test
    fun `getStatus should call receipt service when cheque needed`() {
        val paymentStatus = PaymentStatus()
        paymentStatus.stateId = "SUCCESS"

        val paymentBankId = "test123"
        val traceId = "test-trace"
        val order =
            Order().apply {
                code = "TEST-ORDER"
                recipientEmail = "test@example.com"
            }
        val payment =
            Payment().apply {
                this.paymentBankId = paymentBankId
                stateId = paymentStatus
                this.orderId = order
                this.id = 1L
            }

        `when`(paymentRepository.findByPaymentBankId(paymentBankId)).thenReturn(payment)
        `when`(paymentRepository.findById(payment.id!!)).thenReturn(Optional.of(payment))

        doNothing().`when`(receiptService).generateReceipt(order, traceId)

        val response = service.getStatus(paymentBankId, traceId)

        assertThat(response.data?.cheque).isFalse()
    }

    @Test
    fun `getStatus should return WAIT status for REG payment state`() {
        val paymentBankId = "test-payment-id"
        val payment = Payment(
            paymentBankId = paymentBankId,
            stateId = PaymentStatus(stateId = "REG", stateName = "Registered"),
            id = 1L,
            orderId = Order().apply { id = 1 }
        )
        val status = PaymentStatus().apply { stateId = "REG" }

        `when`(paymentRepository.findByPaymentBankId(paymentBankId)).thenReturn(payment)
        `when`(paymentRepository.findById(payment.id!!)).thenReturn(Optional.of(payment))
        `when`(paymentStatusRepository.findByStateId("REG")).thenReturn(status)

        val response = service.getStatus(paymentBankId, traceId)

        assertEquals("REG", response.data?.paymentStatus)
    }

    @Test
    fun `getStatus should throw exception for unknown payment state`() {
        val paymentBankId = "test-payment-id"
        val payment = Payment(
            paymentBankId = paymentBankId,
            stateId = PaymentStatus(stateId = "UNKNOWN", stateName = "Unknown"),
            id = 1L
        )

        `when`(paymentRepository.findByPaymentBankId(paymentBankId)).thenReturn(payment)

        assertThrows<BusinessException> {
            service.getStatus(paymentBankId, traceId)
        }
    }

    @Test
    fun `processPaymentStatusCheck should process AKB payment for akb_rus bank`() {
        val order = Order().apply {
            id = 1
            code = "TEST-ORDER"
        }
        val payment = Payment(
            id = 1L,
            typeId = PaymentType().apply { typeId = "bankCard" },
            bank = Bank().apply { bankId = "akb_rus" },
            paymentBankId = "test-payment",
            stateId = PaymentStatus().apply { stateId = "REG" },
            orderId = order
        )

        val akbResponse = """
        {
            "status": "Closed",
            "prevStatus": "FullyPaid"
        }
    """.trimIndent()

        `when`(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(),
            eq(String::class.java)
        )).thenReturn(ResponseEntity.ok(akbResponse))

        `when`(objectMapper.readValue(akbResponse, PaymentAkbStatusResponse::class.java))
            .thenReturn(PaymentAkbStatusResponse(status = "Closed", prevStatus = "FullyPaid"))
        `when`(orderRepository.findById(any())).thenReturn(Optional.of(Order().apply { id = 1 }))
        `when`(paymentStatusRepository.findByStateId("SUCCESS")).thenReturn(PaymentStatus().apply { stateId = "SUCCESS" })
        `when`(orderStatusRepository.findByStateId("SUCCESS")).thenReturn(OrderStatus().apply { stateId = "SUCCESS" })
        `when`(paymentRepository.save(any(Payment::class.java))).thenReturn(payment)

        service.processPaymentStatusCheck(payment, traceId)

        assertEquals("SUCCESS", payment.stateId?.stateId)
    }
}
