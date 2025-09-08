package service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.data.DataDescriptionAndPremiumAmount
import ru.sogaz.site.paymentService.dto.response.AkbOrderInfo
import ru.sogaz.site.paymentService.dto.response.AkbOrderResponse
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.SslClientProperties
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.AkbBankIntegrationServiceImpl
import java.util.Optional

class PaymentServiceImplAKBTest {
    private val restTemplate: WebConfigRestTemplate = mock()
    private val generatorService: GeneratorService = mock()
    private val paymentRepository: PaymentRepository = mock()
    private val paymentDao: PaymentDao = mock()
    private val subOrderDao: SubOrderDao = mock()
    private val paymentOperationHistoryDao: PaymentOperationHistoryDao = mock()
    private val apiConfigProperty: ApiConfigProperties = mock()
    private val objectMapper: ObjectMapper = mock()
    private val sslClientProperties: SslClientProperties = mock()

    private val service =
        AkbBankIntegrationServiceImpl(
            paymentOperationHistoryDao,
            apiConfigProperty,
            subOrderDao,
            generatorService,
            restTemplate,
            objectMapper,
            paymentDao,
            sslClientProperties,
        )

    @Test
    fun shouldReturnPaymentLinkWhenAKBAPIRespondsSuccessfully() {
        val order =
            Order().apply {
                orderId = "ORD-1"
                recipientEmail = "mail@test.com"
            }
        val clientSystem = ClientSystem().apply { externalSystemCode = "SYS" }
        val subOrder =
            SubOrder(
                subOrderId = "SUB-1",
                orderId = order,
                docType = "DOC",
                contractId = "CONT-1",
                contractNumber = "CN-1",
                clientSystem = clientSystem,
            )

        val descAndPremiumAmountData =
            DataDescriptionAndPremiumAmount(
                premiumAmount = "1000",
                description = "Test description",
            )
        whenever(generatorService.getDescriptionAndPremiumAmount(any(), any<List<SubOrder>>()))
            .thenReturn(descAndPremiumAmountData)

        val mockRestTemplate = mock<RestTemplate>()
        whenever(restTemplate.xpgRestTemplate(any())).thenReturn(mockRestTemplate)

        val akbOrderResponse =
            AkbOrderResponse(
                order =
                    AkbOrderInfo(
                        id = 12345,
                        hppUrl = "http://akb-pay-link",
                    ),
            )
        whenever(
            mockRestTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any(),
                any<org.springframework.core.ParameterizedTypeReference<AkbOrderResponse>>(),
            ),
        ).thenReturn(ResponseEntity.ok(akbOrderResponse))

        whenever(apiConfigProperty.akbUrl).thenReturn("http://fake-akb")
        whenever(apiConfigProperty.backUrlS).thenReturn("http://back-success")

        val payment =
            Payment(
                id = 1L,
                orderId = order,
            )
        whenever(paymentRepository.findById(1L)).thenReturn(Optional.of(payment))

        val response =
            service.initiateAKBPayment(
                urlToReturn = null,
                urlToReturnF = null,
                paymentId = payment.id,
                premiumAmount = "1000",
                order = order,
                subOrder = subOrder,
            )

        assertEquals("http://akb-pay-link", response.body?.data?.paymentPageUrl)
    }

    @Test
    fun shouldThrowBusinessExceptionWhenRestClientFails() {
        val order = Order(orderId = "ORD-1", recipientEmail = "mail@test.com")
        val clientSystem = ClientSystem().apply { externalSystemCode = "SYS" }
        val subOrder =
            SubOrder(
                subOrderId = "SUB-1",
                orderId = order,
                docType = "DOC",
                contractId = "CONT-1",
                contractNumber = "CN-1",
                clientSystem = clientSystem,
            )

        val descAndPremiumAmountData =
            DataDescriptionAndPremiumAmount(
                premiumAmount = "1000",
                description = "Test description",
            )
        whenever(generatorService.getDescriptionAndPremiumAmount(any(), any<List<SubOrder>>()))
            .thenReturn(descAndPremiumAmountData)

        val mockRestTemplate = mock<RestTemplate>()
        whenever(restTemplate.xpgRestTemplate(any())).thenReturn(mockRestTemplate)

        whenever(
            mockRestTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any(),
                any<org.springframework.core.ParameterizedTypeReference<Map<String, Any>>>(),
            ),
        ).thenThrow(RestClientException("AKB API error"))

        whenever(apiConfigProperty.akbUrl).thenReturn("http://fake-akb")
        whenever(apiConfigProperty.backUrlS).thenReturn("http://back-success")

        val payment =
            Payment(
                id = 1L,
                orderId = order,
            )
        whenever(paymentRepository.findById(1L)).thenReturn(Optional.of(payment))

        val ex =
            assertThrows<BusinessException> {
                service.initiateAKBPayment(
                    urlToReturn = null,
                    urlToReturnF = null,
                    paymentId = payment.id,
                    premiumAmount = "1000",
                    order = order,
                    subOrder = subOrder,
                )
            }

        assertEquals(-1101100504, ex.getErrorCode())
    }
}
