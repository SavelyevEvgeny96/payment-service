package ru.sogaz.site.paymentService.service.cardregistry

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentErrors.Companion.CODE_ERROR_REGISTRY_CARD_NOT_AVAILABLE_INFO
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.data.DataPay
import ru.sogaz.site.paymentService.dto.request.PayQueryParams
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.orThrow
import ru.sogaz.site.paymentService.service.CardRegistryService
import ru.sogaz.site.paymentService.service.OrderService
import ru.sogaz.site.paymentService.service.PaymentService
import ru.sogaz.site.paymentService.service.SubOrderService
import ru.sogaz.site.paymentService.service.payment.RegisterPaymentServiceImpl

@Service
class CardRegistryServiceImpl(
    private val orderService: OrderService,
    private val subOrderService: SubOrderService,
    private val paymentService: PaymentService,
) : CardRegistryService {
    override fun registry(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): DataPay {
        val order: Order = createRegistryOrder(unifiedId, payQueryParams, clientId)
        return order.id
            ?.let {
                val dataPay =
                    try {
                        paymentService.createCardPayment(it, payQueryParams)
                    } catch (e: Exception) {
                        orderService.cancelOrder(order)
                        throw BusinessException(CODE_ERROR_REGISTRY_CARD_NOT_AVAILABLE_INFO, getTraceId())
                    }
                orderService.cancelOrderIfPaymentFail(order)
                return dataPay
            }.orThrow {
                InnerException(
                    getTraceId(),
                    RegisterPaymentServiceImpl.ERROR_PAYMENT_PROCESSING,
                )
            }
    }

    @Transactional
    private fun createRegistryOrder(
        unifiedId: String,
        payQueryParams: PayQueryParams,
        clientId: String,
    ): Order {
        // создание записи в DB Orders
        val order: Order = orderService.createRegestryOrder(unifiedId, payQueryParams, clientId)
        // создание записи в DB Suborder
        subOrderService.createSuborder(order, clientId)
        return order
    }
}
