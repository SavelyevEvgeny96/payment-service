package ru.sogaz.site.paymentService.service.v2.operation

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderDao
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.producer.CheckOperationStatusProducer
import ru.sogaz.site.paymentService.service.v2.operation.impl.OperationServiceImpl
import ru.sogaz.site.paymentService.service.v2.order.IdempotentOrderService
import ru.sogaz.site.paymentService.service.v2.order.impl.IdempotentOrderServiceImpl
import java.util.UUID

// @Import(IdempotentOrderMapperImpl::class)
@ExtendWith(MockKExtension::class, SpringExtension::class)
class OperationServiceTest {
    @RelaxedMockK
    private lateinit var checkOperationStatusProducer: CheckOperationStatusProducer

    @MockK
    private lateinit var idempotentOrderDao: IdempotentOrderDao

    @RelaxedMockK
    private lateinit var idempotentOrderOperationMapper: IdempotentOrderOperationMapper

    @MockK
    private lateinit var idempotentOrderOperationDao: IdempotentOrderOperationDao

    private lateinit var idempotentOrderService: IdempotentOrderService

    private lateinit var operationService: OperationService

    @RelaxedMockK
    private lateinit var payOperationRequest: CardPayOperationRequest

    @BeforeEach
    fun beforeEach() {
        idempotentOrderService =
            IdempotentOrderServiceImpl(
                idempotentOrderDao,
                idempotentOrderOperationMapper,
                idempotentOrderOperationDao,
            )

        operationService =
            OperationServiceImpl(
                idempotentOrderService,
                checkOperationStatusProducer,
            )

        every { idempotentOrderDao.save(any()) } returnsArgument 0
        every { idempotentOrderOperationDao.save(any()) } returnsArgument 0

        every { payOperationRequest.orderId } returns UUID.randomUUID()
    }

//    @Test
//    fun `create payOperation`() {
//        every { idempotentOrderDao.findIdempotentOrderByOrderId(any()) } returns mockk()
//        every { payOperationRequest.params.depersonalization } returns true
//
//        operationService
//
//        val savedOperation = operationService.pay(payOperationRequest)
//
//        assertThat(savedOperation)
//            .returns(true, IdempotentOrderOperation::depersonalization)
//            .returns(payOperationRequest.operationType, IdempotentOrderOperation::operationType)
//            .returns(payOperationRequest.paymentType, IdempotentOrderOperation::paymentType)
//    }
}
