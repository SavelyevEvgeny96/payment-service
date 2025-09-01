package service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.amqp.rabbit.core.RabbitTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentService.config.WebConfigRestTemplate
import ru.sogaz.site.paymentService.dao.OrderStatusDao
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dao.PaymentOperationHistoryDao
import ru.sogaz.site.paymentService.dao.PaymentStatusDao
import ru.sogaz.site.paymentService.dao.SubOrderDao
import ru.sogaz.site.paymentService.entity.OrderStatus
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.repository.ActionTypeRepository
import ru.sogaz.site.paymentService.repository.OrderRepository
import ru.sogaz.site.paymentService.repository.OrderStatusRepository
import ru.sogaz.site.paymentService.repository.PaymentOperationHistoryRepository
import ru.sogaz.site.paymentService.repository.PaymentRepository
import ru.sogaz.site.paymentService.repository.PaymentStatusRepository
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.impl.PaymentStatusCheckerServiceImpl

class CheckStatusOrderTest {
    private val orderRepository: OrderRepository = mock()
    private val paymentRepository: PaymentRepository = mock()

    private val paymentStatusRepository: PaymentStatusRepository = mock()
    private val operationHistoryRepository: PaymentOperationHistoryRepository = mock()
    private val restTemplate: WebConfigRestTemplate = mock()
    private val subOrderDao: SubOrderDao = mock()
    private val apiConfigProperty: ApiConfigProperties = mock()
    private val receiptService: ReceiptService = mock()
    private val orderStatusRepository: OrderStatusRepository = mock()
    private val rabbitTemplate: RabbitTemplate = mock()
    private val objectMapper: ObjectMapper = mock()
    private val rabbit: RabbitProperties = mock()
    private val paymentDao: PaymentDao = mock()
    private val paymentStatusDao: PaymentStatusDao = mock()
    private val orderStatusDao: OrderStatusDao = mock()
    private val operationHistoryDao: PaymentOperationHistoryDao = mock()

    private val service =
        PaymentStatusCheckerServiceImpl(
            paymentDao,
            restTemplate,
            apiConfigProperty,
            receiptService,
            rabbitTemplate,
            objectMapper,
            rabbit,
            subOrderDao,
            paymentStatusDao,
            orderStatusDao,
            orderRepository,
            operationHistoryDao,
            paymentRepository,
        )
    private val errorCodePaidFor = 1001
    private val errorCodeNotAvailable = 2002

    @Test
    fun `should throw BusinessException with errorCodePaidFor when status SUCCESS`() {
        val orderStatus = OrderStatus().apply { stateId = StatusEnum.SUCCESS.value }

        val ex =
            assertThrows<BusinessException> {
                service.checkStatusOrder(orderStatus, errorCodePaidFor, errorCodeNotAvailable)
            }

        assert(ex.getErrorCode() == errorCodePaidFor)
    }

    @Test
    fun `should throw BusinessException with errorCodeNotAvailable when status OVERDUE`() {
        val orderStatus = OrderStatus().apply { stateId = StatusEnum.OVERDUE.value }

        val ex =
            assertThrows<BusinessException> {
                service.checkStatusOrder(orderStatus, errorCodePaidFor, errorCodeNotAvailable)
            }

        assert(ex.getErrorCode() == errorCodeNotAvailable)
    }

    @Test
    fun `should throw BusinessException with errorCodeNotAvailable when status MARKEDDEL`() {
        val orderStatus = OrderStatus().apply { stateId = StatusEnum.MARKEDDEL.value }

        val ex =
            assertThrows<BusinessException> {
                service.checkStatusOrder(orderStatus, errorCodePaidFor, errorCodeNotAvailable)
            }

        assert(ex.getErrorCode() == errorCodeNotAvailable)
    }

    @Test
    fun `should not throw exception when status NEW`() {
        val orderStatus = OrderStatus().apply { stateId = StatusEnum.NEW.value }

        service.checkStatusOrder(orderStatus, errorCodePaidFor, errorCodeNotAvailable)
    }

    @Test
    fun `should not throw exception when orderStatus is null`() {
        service.checkStatusOrder(null, errorCodePaidFor, errorCodeNotAvailable)
    }
}
