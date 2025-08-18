package service
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.service.ConfigDataService
import ru.sogaz.site.paymentService.service.impl.GeneratorServiceImpl

class GeneratorServiceImplTest {
    private lateinit var configDataService: ConfigDataService
    private lateinit var service: GeneratorServiceImpl

    companion object {
        const val DESC_POLICY_NUMBER = "Номера полиса №"
        const val DESC_INSURANCE_CONTRACT = "Страхового договора №"
        const val DESC = "Оплата: "
    }

    @BeforeEach
    fun setUp() {
        configDataService = mock()
        service = GeneratorServiceImpl(configDataService)
    }

    @Test
    fun `generateDescription builds correct description with policy numbers and contracts`() {
        val subOrders =
            listOf(
                SubOrder(policyNumber = "PN1", contractId = "CID1"),
                SubOrder(policyNumber = "PN2", contractId = "0"),
                SubOrder(policyNumber = null, contractId = "CID2"),
                SubOrder(policyNumber = "0", contractId = null),
            )

        val description = service.generateDescription(subOrders)

        // Проверяем, что описание содержит нужные части
        assertTrue(description.contains(DESC))
        assertTrue(description.contains(DESC_POLICY_NUMBER))
        assertTrue(description.contains("PN1"))
        assertTrue(description.contains("PN2"))
        assertTrue(description.contains(DESC_INSURANCE_CONTRACT))
        assertTrue(description.contains("CID1"))
        assertTrue(description.contains("CID2"))
        assertTrue(description.contains("("))
        assertTrue(description.contains(")"))
    }

    @Test
    fun `generateDescription returns only base description if lists are empty`() {
        val subOrders =
            listOf(
                SubOrder(policyNumber = "0", contractId = "0"),
                SubOrder(policyNumber = "", contractId = ""),
                SubOrder(policyNumber = null, contractId = null),
            )

        val description = service.generateDescription(subOrders)

        assertEquals(DESC, description)
    }

    @Test
    fun `generateUniquePaymentId returns non empty UUID string`() {
        val id = service.generateUniquePaymentId()
        assertNotNull(id)
        assertTrue(id.isNotEmpty())
    }
}
