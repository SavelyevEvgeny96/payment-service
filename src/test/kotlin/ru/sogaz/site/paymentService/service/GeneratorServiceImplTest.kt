package ru.sogaz.site.paymentService.service
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.service.payment.GeneratorServiceImpl
import java.time.Duration

class GeneratorServiceImplTest {
    private lateinit var service: GeneratorServiceImpl

    companion object {
        const val DESC_POLICY_NUMBER = "Номера полиса №"
        const val DESC_INSURANCE_CONTRACT = "Страхового договора №"
        const val DESC = "Оплата: "
    }

    @BeforeEach
    fun setUp() {
        service = GeneratorServiceImpl()
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

    @Test
    fun `formatDuration returns seconds only if less than 1 minute`() {
        val duration = Duration.ofMillis(12_345)
        val formatted = service.formatDuration(duration)
        assertEquals("12.3s", formatted)
    }

    @Test
    fun `formatDuration returns minutes and seconds if more than 1 minute`() {
        val duration = Duration.ofMillis(83_700)
        val formatted = service.formatDuration(duration)
        assertEquals("1m23.7s", formatted)
    }

    @Test
    fun `formatDuration returns exactly 0s for zero duration`() {
        val duration = Duration.ZERO
        val formatted = service.formatDuration(duration)
        assertEquals("0.0s", formatted)
    }

    @Test
    fun `jsonHeaders sets content type to application json`() {
        val headers = service.jsonHeaders()
        assertEquals(MediaType.APPLICATION_JSON, headers.contentType)
    }

    @Test
    fun `jsonHeaders returns non null HttpHeaders`() {
        val headers = service.jsonHeaders()
        assertEquals(false, headers.isEmpty())
    }
}
