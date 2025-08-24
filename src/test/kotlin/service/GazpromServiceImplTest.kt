package service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.ActionTypeDao
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dto.request.PaymentPayRequest
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl.Companion.GET_PAYMENT_LINK
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl
import java.util.Optional

class GazpromServiceImplTest {
    private val actionTypeDao: ActionTypeDao = mock()
    private val paymentRepository: PaymentRepository = mock()
    private val apiConfigProperty: ApiConfigProperties = mock()
    private val objectMapper = ObjectMapper()
    private val subOrderRepository: SubOrderRepository = mock()
    private val operationHistoryRepository: PaymentOperationHistoryRepository = mock()
    private val restTemplateWrapper: WebConfigRestTemplate = mock()
    private val restTemplate: RestTemplate = mock()
    private val paymentStatusDao: PaymentStatusDao = mock()
    private val paymentDao: PaymentDao = mock()
    private val generatorService: GeneratorService = mock()

    private lateinit var service: GazpromServiceImpl

    @BeforeEach
    fun setUp() {
        whenever(restTemplateWrapper.restTemplate()).thenReturn(restTemplate)
        whenever(apiConfigProperty.gpbUrl).thenReturn("http://fake-gpb/")
        whenever(apiConfigProperty.portalId).thenReturn("portal123")

        service =
            GazpromServiceImpl(
                generatorService,
                actionTypeDao,
                paymentRepository,
                apiConfigProperty,
                objectMapper,
                subOrderRepository,
                operationHistoryRepository,
                restTemplateWrapper,
                paymentStatusDao,
                paymentDao,
            )
    }

    @Test
    fun `should return token when GPB API responds successfully`() {
        val responseJson = """{ "${PaymentServiceImpl.GPB_TOKEN_ROW}": "abc123" }"""
        whenever(
            restTemplate.exchange(any<String>(), eq(HttpMethod.POST), isNull(), eq(String::class.java)),
        ).thenReturn(ResponseEntity.ok(responseJson))

        val order = mock<Order>()
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
        val token = service.getGPBToken(order, subOrder)

        assert(token == "abc123")
        verify(operationHistoryRepository, never()).save(any())
    }

    @Test
    fun `should return payment link when GPB SBP API responds successfully`() {
        val traceId = "trace-123"
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

        val actionType = mock<ru.sogaz.site.paymentService.entity.ActionType>()
        whenever(actionTypeDao.getActionType(traceId, GET_PAYMENT_LINK)).thenReturn(actionType)
        whenever(subOrderRepository.findAllByOrderId(order)).thenReturn(listOf(subOrder))
        whenever(generatorService.generateDescription(anyList())).thenReturn("Test description")

        val apiResponse =
            mapOf(
                "data" to mapOf("payload" to "http://pay-link"),
            )
        whenever(
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.POST),
                any(),
                any<org.springframework.core.ParameterizedTypeReference<Map<String, Any>>>(),
            ),
        ).thenReturn(ResponseEntity.ok(apiResponse))

        whenever(apiConfigProperty.gpbSbpUrl).thenReturn("http://fake-sbp")
        whenever(apiConfigProperty.paymentAccount).thenReturn("acc123")
        whenever(apiConfigProperty.merchantIdSbpGpb).thenReturn("mrc123")
        whenever(apiConfigProperty.callbackUrlSbp).thenReturn("http://callback")

        val paymentRequest =
            PaymentPayRequest("code", null, null)
        val payment =
            Payment(
                id = 1L,
                orderId = order,
            )
        whenever(paymentRepository.findById(1L)).thenReturn(Optional.of(payment))
        val response = service.initiateGPBSBPPayment(paymentRequest, payment.id, "1000", order, subOrder)

        assert(response.body?.data?.paymentPageUrl == "http://pay-link")
        verify(operationHistoryRepository).save(any())
    }
}
