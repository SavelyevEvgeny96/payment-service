package service
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.service.impl.GeneratorServiceImpl
import ru.sogaz.site.paymentService.util.Util

class GeneratorServiceImplTest {
    private lateinit var util: Util
    private lateinit var service: GeneratorServiceImpl

    @BeforeEach
    fun setUp() {
        util = mock()
        service = GeneratorServiceImpl(util)
    }

    @Test
    fun `generateUniquePaymentCode returns uppercase string of correct length`() {
        val traceId = "trace-1"
        val expectedLength = 10

        whenever(util.getCodeLength(traceId)).thenReturn(expectedLength)

        val code = service.generateUniquePaymentCode(traceId)

        assertNotNull(code)
        assertEquals(expectedLength, code.length)
        assertTrue(code.all { it.isUpperCase() || it.isDigit() })
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
        assertTrue(description.contains(Util.DESC))
        assertTrue(description.contains(Util.DESC_POLICY_NUMBER))
        assertTrue(description.contains("PN1"))
        assertTrue(description.contains("PN2"))
        assertTrue(description.contains(Util.DESC_INSURANCE_CONTRACT))
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

        assertEquals(Util.DESC, description)
    }

    @Test
    fun `generateUniquePaymentId returns non empty UUID string`() {
        val id = service.generateUniquePaymentId()
        assertNotNull(id)
        assertTrue(id.isNotEmpty())
    }
}
