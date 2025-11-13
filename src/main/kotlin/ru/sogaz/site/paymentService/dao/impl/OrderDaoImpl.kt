package ru.sogaz.site.paymentService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_GET_STATUS_ORDER
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dao.OrderDao
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.repository.OrderRepository
import java.time.LocalDateTime
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Repository
class OrderDaoImpl(
    private val orderRepository: OrderRepository,
) : OrderDao {
    private val logger = loggerFor(javaClass)

    companion object {
        private const val LOG_ERROR_ORDER_SAVE = "Не удалось сохранить данные по заказу"
        private const val LOG_ORDER_STATUS_NOT_FOUND = "Статус заказа с не найден для TraceId: "
    }

    override fun getOrderId(orderId: String): Order =
        try {
            orderRepository.findById(UUID.fromString(orderId)).get()
        } catch (e: Exception) {
            logger.error(LOG_ORDER_STATUS_NOT_FOUND, e)
            throw BusinessException(CODE_ERROR_GET_STATUS_ORDER, getTraceId())
        }

    override fun findById(orderId: UUID): Order? =
        orderRepository
            .findById(orderId)
            .getOrNull()

    override fun save(order: Order): Order {
        try {
            return orderRepository.save(order)
        } catch (e: Exception) {
            logger.error(LOG_ERROR_ORDER_SAVE, e)
            throw InnerException(getTraceId(), LOG_ERROR_ORDER_SAVE + e.message)
        }
    }

    override fun renewUpdateDate(order: Order): Order =
        order
            .apply { updateDate = LocalDateTime.now() }
            .run(::save)
}
