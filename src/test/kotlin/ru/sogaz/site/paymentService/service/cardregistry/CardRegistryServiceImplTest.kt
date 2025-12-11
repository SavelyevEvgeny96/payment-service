package ru.sogaz.site.paymentService.service.cardregistry

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.SubOrderService
import java.net.URI
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CardRegistryServiceImplTest {

    @MockK
    private lateinit var orderService: OrderService

    @RelaxedMockK
    private lateinit var subOrderService: SubOrderService

    @MockK
    private lateinit var paymentService: PaymentService

    @InjectMockKs
    private lateinit var cardRegistryServiceImpl: CardRegistryServiceImpl

    @Test
    fun `registry should return DataPay`() {
        val dataPay: DataPay = DataPay(URI("url"))
        val order: Order = Order(id = UUID.randomUUID())
        every { paymentService.createCardPayment(any(UUID::class), any(PayQueryParams::class)) }.returns(dataPay)
        every {
            orderService.createRegestryOrder(
                any(String::class),
                any(PayQueryParams::class),
                any(String::class)
            )
        }.returns(order)
        justRun { orderService.cancelOrderIfPaymentFail(any(Order::class)) }

        val result = cardRegistryServiceImpl.registry("unifiedId", PayQueryParams(), "site")

        verify { orderService.cancelOrderIfPaymentFail(order) }
        assertEquals(dataPay, result)
    }
}