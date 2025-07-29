package service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
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
import ru.sogaz.site.paymentService.dao.GetActionTypeDao
import ru.sogaz.site.paymentService.dao.GetPaymentDao
import ru.sogaz.site.paymentService.dao.GetPaymentStatusDao
import ru.sogaz.site.paymentService.entity.ClientSystem
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.impl.GazpromServiceImpl
import ru.sogaz.site.paymentService.service.impl.PaymentServiceImpl
import ru.sogaz.site.paymentService.util.Util

class GazpromServiceImplTest {

    private val getActionTypeDao: GetActionTypeDao = mock()
    private val actionTypeRepository: ActionTypeRepository = mock()
    private val paymentRepository: PaymentRepository = mock()
    private val apiConfigProperty: ApiConfigProperties = mock()
    private val objectMapper = ObjectMapper()
    private val subOrderRepository: SubOrderRepository = mock()
    private val operationHistoryRepository: PaymentOperationHistoryRepository = mock()
    private val restTemplateWrapper: WebConfigRestTemplate = mock()
    private val restTemplate: RestTemplate = mock()
    private val util: Util = mock()
    private val getPaymentStatusDao: GetPaymentStatusDao = mock()
    private val getPaymentDao: GetPaymentDao = mock()

    private lateinit var service: GazpromServiceImpl

    @BeforeEach
    fun setUp() {
        whenever(restTemplateWrapper.restTemplate()).thenReturn(restTemplate)
        whenever(apiConfigProperty.gpbUrl).thenReturn("http://fake-gpb/")
        whenever(apiConfigProperty.portalId).thenReturn("portal123")

        service = GazpromServiceImpl(
            getActionTypeDao,
            paymentRepository,
            apiConfigProperty,
            objectMapper,
            subOrderRepository,
            operationHistoryRepository,
            restTemplateWrapper,
            util,
            getPaymentStatusDao,
            getPaymentDao
        )
    }

    @Test
    fun `should return token when GPB API responds successfully`() {
        val responseJson = """{ "${PaymentServiceImpl.GPB_TOKEN_ROW}": "abc123" }"""
        whenever(
            restTemplate.exchange(any<String>(), eq(HttpMethod.POST), isNull(), eq(String::class.java))
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
        val token = service.getGPBToken("trace-1", order, subOrder)


        assert(token == "abc123")
        verify(operationHistoryRepository, never()).save(any())
    }
}