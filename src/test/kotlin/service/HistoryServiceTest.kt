package service

import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.ActionType
import ru.sogaz.site.paymentService.enums.ExternalSystemCodeEnum
import ru.sogaz.site.paymentService.service.order.HistoryServiceImpl
import kotlin.test.Test

class HistoryServiceTest {
    private lateinit var historyService: HistoryServiceImpl
    private val subOrderDao = mock<SubOrderDao>()
    private val operationHistoryDao = mock<PaymentOperationHistoryDao>()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        historyService = HistoryServiceImpl(subOrderDao, operationHistoryDao)
    }

    @Test
    fun `should create order history record and save it to DAO`() {
        val order = Order()
        val traceId = "trace123"
        val subOrders = listOf(SubOrder(externalSystemCode = ExternalSystemCodeEnum.ADI))
        val actionType = ActionType.ORDER_PAID.value

        doReturn(subOrders).`when`(subOrderDao).getAllSubOrderListByOrderId(order, traceId)

        historyService.createOrderHistoryRecord(order, traceId)

        verify(operationHistoryDao).saveRecordOperationHistory(
            order,
            subOrders.first().externalSystemCode,
            traceId,
            actionType,
        )
    }
}
