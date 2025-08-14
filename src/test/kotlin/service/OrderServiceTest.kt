package service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.BankDao
import ru.sogaz.site.paymentService.dao.GetClientSystemDao
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dto.DataGetOrderStatus
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.GeneratorService
import ru.sogaz.site.paymentService.service.impl.OrderServiceImpl
import ru.sogaz.siter.models.resonses.Response

class OrderServiceTest {
    private val traceId = "trace-123"
    private val orderId = "order-456"
    private val orderRepository = mock<OrderRepository>()
    private val apiConfigProperty = mock<ApiConfigProperties>()
    private val generatorService = mock<GeneratorService>()
    private val subOrderRepository = mock<SubOrderRepository>()
    private val bankDao = mock<BankDao>()
    private val getClientSystemDao = mock<GetClientSystemDao>()
    private val orderStatusDao = mock<OrderStatusDao>()

    @Test
    fun `getOrderStatus should return success when order exists`() {
        val orderDao =
            mock<OrderDao>().apply {
                `when`(getOrderId(traceId, orderId)).thenReturn(
                    Order().apply {
                        orderStatus =
                            OrderStatus().apply {
                                stateId = "NEW"
                            }
                    },
                )
            }
        val service =
            OrderServiceImpl(
                orderDao = orderDao,
                orderRepository = orderRepository,
                apiConfigProperty = apiConfigProperty,
                generatorService = generatorService,
                subOrderRepository = subOrderRepository,
                bankDao = bankDao,
                getClientSystemDao = getClientSystemDao,
                orderStatusDao = orderStatusDao,
            )
        val response: Response<DataGetOrderStatus> = service.getOrderStatus(orderId)
        assertThat(response.data?.orderStatus).isEqualTo("NEW")
        assertThat(response.code).isEqualTo(1201503200)
    }

    @Test
    fun `getOrderStatus should throw InnerException when repository fails`() {
        val orderDao = mock<OrderDao>()
        `when`(orderDao.getOrderId(traceId, orderId))
            .thenThrow(InnerException(traceId, "DB error"))
        val orderRepository = mock<OrderRepository>()
        val service =
            OrderServiceImpl(
                orderDao = orderDao,
                orderRepository = orderRepository,
                apiConfigProperty = apiConfigProperty,
                generatorService = generatorService,
                subOrderRepository = subOrderRepository,
                bankDao = bankDao,
                getClientSystemDao = getClientSystemDao,
                orderStatusDao = orderStatusDao,
            )
        assertThrows<InnerException> {
            service.getOrderStatus(orderId)
        }
    }
}
