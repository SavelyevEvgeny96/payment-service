package service

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.PaymentStatus
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.PaymentStatusCheckerServiceImpl
import java.util.Optional

class PaymentStatusCheckerServiceTest {
    private val paymentRepository = mock<PaymentRepository>()
    private val receiptService = mock<ReceiptService>()
    private val orderRepository = mock<OrderRepository>()
    private val restTemplate = mock<WebConfigRestTemplate>()
    private val operationHistoryRepository = mock<PaymentOperationHistoryRepository>()
    private val paymentStatusRepository = mock<PaymentStatusRepository>()
    private val apiConfigProperty = mock<ApiConfigProperties>()
    private val orderStatusRepository = mock<OrderStatusRepository>()
    private val rabbitTemplate = mock<RabbitTemplate>()
    private val objectMapper = mock<ObjectMapper>()
    private val rabbitProperties = mock<RabbitProperties>()
    private val subOrderDao = mock<SubOrderDao>()
    private val service =
        PaymentStatusCheckerServiceImpl(
            orderRepository = orderRepository,
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
            subOrderDao = subOrderDao,
        )

    private val traceId = "test-trace-id"

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

        `when`(paymentRepository.findByPaymentBankId(paymentBankId)).thenReturn(payment)
        `when`(paymentRepository.findById(payment.id!!)).thenReturn(Optional.of(payment))

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

        `when`(paymentRepository.findByPaymentBankId(paymentBankId)).thenReturn(payment)
        `when`(paymentRepository.findById(payment.id!!)).thenReturn(Optional.of(payment))

        doNothing().`when`(receiptService).generateReceipt(order)

        val response = service.getStatus(paymentBankId)

        assertThat(response.data?.cheque).isFalse()
    }
}
