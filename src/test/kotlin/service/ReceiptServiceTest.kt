package service

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentService.dto.response.PaymentData
import ru.sogaz.site.paymentService.dto.response.PaymentReceiptCreateResponse
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.properties.ReceiptProperties
import ru.sogaz.site.paymentService.repository.ChequeSentRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.impl.ReceiptServiceImpl
import java.util.UUID

class ReceiptServiceTest {
    private val receiptProperty = mock<ReceiptProperties>()
    private val restTemplate = mock<WebConfigRestTemplate>()
    private val subOrderRepository = mock<SubOrderRepository>()
    private val operationHistoryRepository = mock<PaymentOperationHistoryRepository>()
    private val objectMapper = mock<ObjectMapper>()
    private val paymentRepository = mock<PaymentRepository>()
    private val chequeSentRepository = mock<ChequeSentRepository>()
    private val subOrderDao = mock<SubOrderDao>()

    private val service =
        ReceiptServiceImpl(
            operationHistoryRepository = operationHistoryRepository,
            receiptProperty = receiptProperty,
            restTemplate = restTemplate,
            objectMapper = objectMapper,
            paymentRepository = paymentRepository,
            chequeSentRepository = chequeSentRepository,
            subOrderDao = subOrderDao,
        )

    private val traceId = "test-trace-id"

    @Test
    fun `generateReceipt should create valid request`() {
        val clientSystems =
            ClientSystem().apply {
                externalSystemCode = "TEST_SYSTEM"
            }
        val subOrder =
            SubOrder().apply {
                policyNumber = "POL123"
                contractId = "CONT123"
                premiumAmount = "1000.00"
                clientSystem = clientSystems
            }

        val order =
            Order().apply {
                orderId = "ORDER-123"
                premiumAmount = "1000.00"
                recipientEmail = "test@example.com"
            }

        `when`(subOrderRepository.findAllByOrderId(order)).thenReturn(listOf(subOrder))
        `when`(subOrderRepository.findFirstByOrderId(order)).thenReturn(subOrder)
        `when`(paymentRepository.findByOrderId(order)).thenReturn(
            Payment().apply {
                id = 12
            },
        )
        `when`(receiptProperty.receiptUrl).thenReturn("http://test.url")

        val mockResponse =
            PaymentReceiptCreateResponse(
                "SUCCESS",
                200,
                "222",
                null,
                null,
                UUID.randomUUID().toString(),
                null,
                PaymentData("222", "222"),
            )
        val mockSpringRestTemplate = mock<RestTemplate>()
        whenever(restTemplate.defaultRestTemplate()).thenReturn(mockSpringRestTemplate)
        `when`(
            restTemplate.defaultRestTemplate().exchange(
                any(String::class.java),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                eq(PaymentReceiptCreateResponse::class.java),
            ),
        ).thenReturn(ResponseEntity(mockResponse, HttpStatus.OK))

        verify(mockSpringRestTemplate, atLeastOnce()).exchange(
            eq(receiptProperty.receiptUrl),
            eq(HttpMethod.POST),
            ArgumentCaptor.forClass(HttpEntity::class.java).capture(),
            eq(PaymentReceiptCreateResponse::class.java),
        )
        service.generateReceipt(order)

        val captor = ArgumentCaptor.forClass(HttpEntity::class.java)
        verify(restTemplate).defaultRestTemplate().exchange(
            eq(receiptProperty.receiptUrl),
            eq(HttpMethod.POST),
            captor.capture(),
            eq(PaymentReceiptCreateResponse::class.java),
        )

        val request = captor.value.body as PaymentReceiptCreateRequest
        assertThat(request.client.email).isEqualTo("test@example.com")
        assertThat(request.system).isEqualTo("Atol")
    }

    @Test
    fun `generateReceipt should handle API failure response`() {
        val clientSystems =
            ClientSystem().apply {
                externalSystemCode = "TEST_SYSTEM"
            }
        val subOrder =
            SubOrder().apply {
                policyNumber = "POL123"
                contractId = "CONT123"
                premiumAmount = "1000.00"
                clientSystem = clientSystems
            }

        val order =
            Order().apply {
                orderId = "ORDER-123"
                premiumAmount = "1000.00"
                recipientEmail = "test@example.com"
            }

        `when`(subOrderRepository.findAllByOrderId(order)).thenReturn(listOf(subOrder))
        `when`(subOrderRepository.findFirstByOrderId(order)).thenReturn(subOrder)
        `when`(paymentRepository.findByOrderId(order)).thenReturn(
            Payment().apply {
                id = 12
            },
        )
        `when`(receiptProperty.receiptUrl).thenReturn("http://test.url")

        val mockResponse = """{"status":"FAILED"}"""
        `when`(objectMapper.readValue(mockResponse, PaymentReceiptCreateResponse::class.java))
            .thenReturn(
                PaymentReceiptCreateResponse(
                    "FAILED",
                    500,
                    traceId,
                    null,
                    null,
                    UUID.randomUUID().toString(),
                ),
            )

        `when`(
            restTemplate.defaultRestTemplate().exchange(
                any(String::class.java),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                eq(String::class.java),
            ),
        ).thenReturn(ResponseEntity(mockResponse, HttpStatus.OK))

        assertThrows<InnerException> {
            service.generateReceipt(order)
        }
    }

    @Test
    fun `generateReceipt should handle REST client exception`() {
        val clientSystems =
            ClientSystem().apply {
                externalSystemCode = "TEST_SYSTEM"
            }

        val subOrder =
            SubOrder().apply {
                policyNumber = "POL123"
                contractId = "CONT123"
                premiumAmount = "1000.00"
                clientSystem = clientSystems
            }

        val order =
            Order().apply {
                orderId = "ORDER-123"
                premiumAmount = "1000.00"
                recipientEmail = "test@example.com"
            }

        `when`(subOrderRepository.findAllByOrderId(order)).thenReturn(listOf(subOrder))
        `when`(subOrderRepository.findFirstByOrderId(order)).thenReturn(subOrder)
        `when`(paymentRepository.findByOrderId(order)).thenReturn(
            Payment().apply {
                id = 12
            },
        )
        `when`(receiptProperty.receiptUrl).thenReturn("http://test.url")

        `when`(
            restTemplate.defaultRestTemplate().exchange(
                any(String::class.java),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                eq(String::class.java),
            ),
        ).thenThrow(RestClientException("API error"))

        val exception =
            assertThrows<InnerException> {
                service.generateReceipt(order)
            }
        assertThat(exception.message).contains(ReceiptServiceImpl.ERROR_RECEIPT_GENERATION)
    }
}
