package ru.sogaz.site.paymentService.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mapstruct.factory.Mappers
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.dto.data.DataGetOrderStatus
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import ru.sogaz.site.paymentService.dto.request.SubOrderRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.mapper.order.OrderManualMapper
import ru.sogaz.site.paymentService.mapper.order.OrderMapper
import ru.sogaz.site.paymentService.service.order.OrderServiceImpl
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    companion object {
        const val TRACE_ID = "trace-123"
        const val VALID_ORDER_ID = "order-456"
        const val INVALID_ORDER_ID = "invalid"
    }

    @MockK
    private lateinit var orderDao: OrderDao

    private lateinit var orderMapper: OrderMapper
    private lateinit var orderManualMapper: OrderManualMapper
    private lateinit var orderService: OrderService

    @BeforeEach
    fun beforeEach() {
        // Поведение orderDao
        every { orderDao.getOrderId(VALID_ORDER_ID) } answers { Order() }
        every { orderDao.getOrderId(INVALID_ORDER_ID) } throws InnerException(TRACE_ID, "DB error")

        // Настройка реального маппера и ручного маппера
        orderMapper = Mappers.getMapper(OrderMapper::class.java)
        orderManualMapper = OrderManualMapper(orderMapper)

        // Сервис с внедренным OrderManualMapper
        orderService =
            OrderServiceImpl(
                orderDao = orderDao,
                orderManualMapper = orderManualMapper,
            )
    }

    /** -----------------------------
     *  TEST: getOrderStatus
     *  ----------------------------- */
    @Test
    fun getOrderStatus_should_return_success_when_order_exists() {
        assertThat(orderService.getOrderStatus(VALID_ORDER_ID))
            .returns("NEW", DataGetOrderStatus::orderStatus)
    }

    @Test
    fun getOrderStatus_should_throw_InnerException_when_repository_fails() {
        assertThrows<InnerException> {
            orderService.getOrderStatus(INVALID_ORDER_ID)
        }
    }

    /** -----------------------------
     *  TEST: makeOrderByRequest
     *  ----------------------------- */
    @Test
    fun makeOrderByRequest_should_create_order_with_suborders_and_sum_premium() {
        // given: SubOrderRequest — все обязательные поля заполнены
        val sub1 =
            SubOrderRequest(
                premiumAmount = BigDecimal("100.10"),
                contractNumber = "CN-1",
                contractId = "CID-1",
                contractDate = Instant.now(),
                managerEmail = "a@b.com",
                channel = "WEB",
            )

        val sub2 =
            SubOrderRequest(
                premiumAmount = BigDecimal("200.20"),
                contractNumber = "CN-2",
                contractId = "CID-2",
                contractDate = Instant.now(),
                managerEmail = "a@b.com",
                channel = "WEB",
            )

        val orderReq =
            OrderRequest(
                orders = mutableListOf(sub1, sub2),
                orderEndDate = Instant.now().plusSeconds(3600),
                recipientEmail = "client@mail.ru",
                bank = BankEnum.GPB,
                urlToReturn = "https://return",
                urlToDecline = "https://decline",
                clientId = "CLIENT-1",
            )

        // act
        val order = orderService.makeOrderByRequest(orderReq)

        // assert: базовые поля
        assertThat(order).isNotNull
        assertThat(order.clientId).isEqualTo("CLIENT-1")

        // SUBORDERS
        assertThat(order.subOrders).hasSize(2)
        val s1 = order.subOrders[0]
        val s2 = order.subOrders[1]
        assertThat(s1.order).isEqualTo(order)
        assertThat(s2.order).isEqualTo(order)
        assertThat(s1.premiumAmount).isEqualTo("100.10")
        assertThat(s2.premiumAmount).isEqualTo("200.20")

        // PREMIUM SUM
        assertThat(order.premiumAmount).isEqualTo("300.30")
    }

    /** -----------------------------
     *  Private helpers
     *  ----------------------------- */
    private fun initOrderService(): OrderServiceImpl =
        OrderServiceImpl(
            orderDao = orderDao,
            orderManualMapper = orderManualMapper,
        )
}
