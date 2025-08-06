package service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.ConfigDataRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.repository.SubOrderRepository
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.PaymentStatusCheckerServiceImpl

class CheckStatusOrderTest {
    private val orderRepository: OrderRepository = mock()
    private val paymentRepository: PaymentRepository = mock()
    private val configDataRepository: ConfigDataRepository = mock()
    private val paymentStatusRepository: PaymentStatusRepository = mock()
    private val operationHistoryRepository: PaymentOperationHistoryRepository = mock()
    private val actionTypeRepository: ActionTypeRepository = mock()
    private val restTemplate: RestTemplate = mock()
    private val subOrderRepository: SubOrderRepository = mock()
    private val apiConfigProperty: ApiConfigProperties = mock()
    private val receiptService: ReceiptService = mock()
    private val orderStatusRepository: OrderStatusRepository = mock()
    private val rabbitTemplate: RabbitTemplate = mock()
    private val objectMapper: ObjectMapper = mock()
    private val rabbit: RabbitProperties = mock()

    private val service =
        PaymentStatusCheckerServiceImpl(
            orderRepository,
            paymentRepository,
            configDataRepository,
            paymentStatusRepository,
            operationHistoryRepository,
            actionTypeRepository,
            restTemplate,
            subOrderRepository,
            apiConfigProperty,
            receiptService,
            orderStatusRepository,
            rabbitTemplate,
            objectMapper,
            rabbit,
        )

    private val errorCodePaidFor = 1001
    private val errorCodeNotAvailable = 2002
    private val traceId = "trace-123"

    @Test
    fun `should throw BusinessException with errorCodePaidFor when status SUCCESS`() {
        val orderStatus = OrderStatus().apply { stateId = StatusEnum.SUCCESS.value }

        val ex =
            assertThrows<BusinessException> {
                service.checkStatusOrder(orderStatus, errorCodePaidFor, errorCodeNotAvailable, traceId)
            }

        assert(ex.getErrorCode() == errorCodePaidFor)
    }

    @Test
    fun `should throw BusinessException with errorCodeNotAvailable when status OVERDUE`() {
        val orderStatus = OrderStatus().apply { stateId = StatusEnum.OVERDUE.value }

        val ex =
            assertThrows<BusinessException> {
                service.checkStatusOrder(orderStatus, errorCodePaidFor, errorCodeNotAvailable, traceId)
            }

        assert(ex.getErrorCode() == errorCodeNotAvailable)
    }

    @Test
    fun `should throw BusinessException with errorCodeNotAvailable when status MARKEDDEL`() {
        val orderStatus = OrderStatus().apply { stateId = StatusEnum.MARKEDDEL.value }

        val ex =
            assertThrows<BusinessException> {
                service.checkStatusOrder(orderStatus, errorCodePaidFor, errorCodeNotAvailable, traceId)
            }

        assert(ex.getErrorCode() == errorCodeNotAvailable)
    }

    @Test
    fun `should not throw exception when status NEW`() {
        val orderStatus = OrderStatus().apply { stateId = StatusEnum.NEW.value }

        service.checkStatusOrder(orderStatus, errorCodePaidFor, errorCodeNotAvailable, traceId)
    }

    @Test
    fun `should not throw exception when orderStatus is null`() {
        service.checkStatusOrder(null, errorCodePaidFor, errorCodeNotAvailable, traceId)
    }
}
