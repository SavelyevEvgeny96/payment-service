package ru.sogaz.site.paymentService.service.order

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import ru.sogaz.site.paymentService.config.ServicesConfig
import ru.sogaz.site.paymentService.service.QueueStatusResultNameNormalizeService
import ru.sogaz.site.paymentService.service.order.QueueStatusResultNameNormalizeServiceImpl.Companion.ORDER_STATUS_PATTERN

@SpringBootTest
@ContextConfiguration(classes = [ServicesConfig::class])
class QueueStatusResultNameNormalizeServiceImplTest {
    @Autowired
    private lateinit var service: QueueStatusResultNameNormalizeService

    @Test
    fun `when call buildQueueStatusResultName with clinentId hyphen then return dot`() {
        val result = service.buildQueueStatusResultName(ORDER_STATUS_PATTERN, "www.api-client.ru")

        assertEquals("payment.status.www.api.client.ru.created", result)
    }
}
