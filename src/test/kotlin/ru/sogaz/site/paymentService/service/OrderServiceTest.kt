package ru.sogaz.site.paymentService.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.service.order.OrderServiceImpl
import ru.sogaz.site.paymentService.service.order.OrderServiceImpl.Companion.STATUS_CODE_SUCCESS_GET_ORDER_STATUS
import ru.sogaz.siter.models.resonses.Response

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    companion object {
        const val TRACE_ID = "trace-123"
        const val VALID_ORDER_ID = "order-456"
        const val INVALID_ORDER_ID = "invalid"
    }

    @RelaxedMockK
    private lateinit var apiConfigProperty: ApiConfigProperties

    @MockK
    private lateinit var orderDao: OrderDao

    private lateinit var orderService: OrderService

    @BeforeEach
    fun beforeEach() {
        every { orderDao.getOrderId(VALID_ORDER_ID) }.answers { Order() }
        every { orderDao.getOrderId(INVALID_ORDER_ID) }.throws(InnerException(TRACE_ID, "DB error"))

        orderService = initOrderService()
    }

    @Test
    fun `getOrderStatus should return success when order exists`() {
        assertThat(orderService.getOrderStatus(VALID_ORDER_ID))
            .returns(STATUS_CODE_SUCCESS_GET_ORDER_STATUS, Response<DataGetOrderStatus>::code)
            .returns("NEW") { res -> res.data?.orderStatus }
    }

    @Test
    fun `getOrderStatus should throw InnerException when repository fails`() {
        assertThrows<InnerException> {
            orderService.getOrderStatus(INVALID_ORDER_ID)
        }
    }

    private fun initOrderService(): OrderServiceImpl =
        OrderServiceImpl(
            orderDao = orderDao,
            apiConfigProperty = apiConfigProperty,
        )
}
