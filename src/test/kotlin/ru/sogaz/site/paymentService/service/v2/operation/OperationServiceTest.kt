package ru.sogaz.site.paymentService.service.v2.operation

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderDao
import ru.sogaz.site.paymentService.dao.v2.IdempotentOrderOperationDao
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.producer.CheckOperationStatusProducer
import ru.sogaz.site.paymentService.service.v2.operation.impl.OperationServiceImpl
import ru.sogaz.site.paymentService.service.v2.operation.inline.step
import ru.sogaz.site.paymentService.service.v2.operation.inline.stepWithSave
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand
import ru.sogaz.site.paymentService.service.v2.order.IdempotentOrderService
import ru.sogaz.site.paymentService.service.v2.order.impl.IdempotentOrderServiceImpl
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
class OperationServiceTest {
    companion object {
        private const val TEST_BANK_ID = "TEST_BANK_ID"
        private const val TEST_PAYMENT_PAGE = "TEST_PAYMENT_PAGE"
    }

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
    private lateinit var cardPayOperationRequest: CardPayOperationRequest

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

        every { cardPayOperationRequest.orderId } returns UUID.randomUUID()
    }

    @Test
    fun `execute command with two steps with save`() {
        every { idempotentOrderDao.findIdempotentOrderByOrderId(any()) } returns mockk()
        every { cardPayOperationRequest.params.depersonalization } returns true
        val testCommand = testCardPayOperationCommand2StepsWithSave()

        val result = operationService.runIdempotentOperation(testCommand).getOrThrow()

        verify(exactly = 3) { idempotentOrderOperationDao.save(any()) }

        assertThat(result)
            .isEqualTo(TEST_PAYMENT_PAGE)
    }

    @Test
    fun `execute command with one step with save and one step without`() {
        every { idempotentOrderDao.findIdempotentOrderByOrderId(any()) } returns mockk()
        every { cardPayOperationRequest.params.depersonalization } returns true

        val testCommand = testCardPayOperationCommand1StepWithSaveAnd1StepWithout()

        val result: String = operationService.runIdempotentOperation(testCommand).getOrThrow()

        verify(exactly = 2) { idempotentOrderOperationDao.save(any()) }

        assertThat(result)
            .isEqualTo(TEST_PAYMENT_PAGE)
    }

    private fun testCardPayOperationCommand2StepsWithSave() =
        OperationCommand(
            request = cardPayOperationRequest,
            requestToOrderOperationMapper = idempotentOrderOperationMapper::toGpbIdempotentOrderOperation,
            strategy = cardPayStrategyWith2StepsWithSave(),
        )

    private fun cardPayStrategyWith2StepsWithSave() =
        cardPayOperationRequest
            .stepWithSave(
                action = { TEST_BANK_ID },
                resultToOrderOperationMapper = { resultBankId -> apply { paymentBankId = resultBankId } },
            ).stepWithSave(
                action = { _ -> TEST_PAYMENT_PAGE },
                resultToOrderOperationMapper = { resultPaymentPage -> apply { paymentBankUrl = resultPaymentPage } },
            )

    private fun testCardPayOperationCommand1StepWithSaveAnd1StepWithout(): OperationCommand<CardPayOperationRequest, String> =
        OperationCommand(
            request = cardPayOperationRequest,
            requestToOrderOperationMapper = idempotentOrderOperationMapper::toGpbIdempotentOrderOperation,
            strategy = cardPayStrategyWith1StepWithSaveAnd1Without(),
        )

    private fun cardPayStrategyWith1StepWithSaveAnd1Without() =
        cardPayOperationRequest
            .stepWithSave(
                action = { TEST_BANK_ID },
                resultToOrderOperationMapper = { resultBankId -> apply { paymentBankId = resultBankId } },
            ).step(
                action = { _ -> TEST_PAYMENT_PAGE },
            )
}
