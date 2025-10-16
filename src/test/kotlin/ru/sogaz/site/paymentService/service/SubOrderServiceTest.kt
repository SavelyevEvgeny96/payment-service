package ru.sogaz.site.paymentService.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.mapper.SubOrderMapper
import ru.sogaz.site.paymentService.service.order.SubOrderServiceImpl
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SubOrderServiceTest {

    @Mock
    lateinit var subOrderDao: SubOrderDao

    @Mock
    lateinit var subOrderMapper: SubOrderMapper

    @InjectMocks
    lateinit var subOrderService: SubOrderServiceImpl

    companion object {
        private const val ORDER_ID = "71d9f197-578f-40b5-afa9-78e4eb8304a2"
    }

    @Test
    fun `should update subOrder successfully`() {
        val updateRequest = buildUpdatePaymentInvoiceRequest()
        val existedSubOrder = SubOrder()
        val expectedSubOrder = buildSubOrder()
        `when`(subOrderDao.findAllByOrderId(updateRequest.orderId)).thenReturn(listOf(existedSubOrder))
        `when`(subOrderMapper.updateSubOrder(updateRequest, existedSubOrder)).thenReturn(expectedSubOrder)
        `when`(subOrderDao.save(expectedSubOrder)).thenReturn(expectedSubOrder)

        val result = subOrderService.updateSubOrder(updateRequest)
        assertEquals(updateRequest.orderId, result.order?.id)
        assertEquals(updateRequest.premiumAmount.toString(), result.premiumAmount)
    }

    private fun buildUpdatePaymentInvoiceRequest(): UpdatePaymentInvoiceRequest {
        return UpdatePaymentInvoiceRequest(
            UUID.fromString(ORDER_ID),
            null,
            BigDecimal.TEN,
            "testemail@gmail.com",
            null,
            true,
        )
    }

    private fun buildSubOrder(): SubOrder {
        return SubOrder(
            id = UUID.fromString(ORDER_ID),
            order = Order(UUID.fromString(ORDER_ID)),
            premiumAmount = BigDecimal.TEN.toString()
        )
    }
}