package ru.sogaz.site.paymentService.service.order

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.sogaz.site.paymentService.service.order.QueueStatusResultNameNormalizeServiceImpl.Companion.ORDER_STATUS_PATTERN

class QueueStatusResultNameNormalizeServiceImplTest {
    @Test
    fun `when call buildQueueStatusResultName with clinentId hyphen then return dot`() {
        val service = QueueStatusResultNameNormalizeServiceImpl()

        val result = service.buildQueueStatusResultName(ORDER_STATUS_PATTERN, "www.api-client.ru")

        assertEquals("payment.status.www.api.client.ru.created", result)
    }
}
