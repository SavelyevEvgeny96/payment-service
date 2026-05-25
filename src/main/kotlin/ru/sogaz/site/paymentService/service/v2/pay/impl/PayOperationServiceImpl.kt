package ru.sogaz.site.paymentService.service.v2.pay.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentQrContent
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayRegOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.enums.OperationBank
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import ru.sogaz.site.paymentService.producer.OperationDetailsProducer
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbCardIntegration
import ru.sogaz.site.paymentService.service.v2.bank.gpb.GpbSbpPayIntegration
import ru.sogaz.site.paymentService.service.v2.operation.OperationService
import ru.sogaz.site.paymentService.service.v2.operation.inline.gpbOperationCommand
import ru.sogaz.site.paymentService.service.v2.operation.inline.onFailure
import ru.sogaz.site.paymentService.service.v2.operation.inline.onFinalState
import ru.sogaz.site.paymentService.service.v2.operation.inline.step
import ru.sogaz.site.paymentService.service.v2.operation.inline.stepWithSave
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand
import ru.sogaz.site.paymentService.service.v2.pay.PayOperationService
import ru.sogaz.site.paymentService.service.v2.rules.RulePaymentTypeService

@Service
class PayOperationServiceImpl(
    private val operationService: OperationService,
    private val gpbCardIntegration: GpbCardIntegration,
    private val gpbSbpIntegration: GpbSbpPayIntegration,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val operationDetailsProducer: OperationDetailsProducer,
    private val rulePaymentTypeService: RulePaymentTypeService,
) : PayOperationService {
    private val logger = loggerFor(javaClass)
    companion object {
        private const val RECURRENT_INTERNAL_ERROR = "Платежная система недоступна"
        private const val OPERATION_NOT_AVAILABLE_ERROR = "Операция недоступна для выбранного способа оплаты"
    }

    /**
     * Формирует команду и стратегию по регистрации и получению платежной страницы в банке ГПБ.
     *
     * @param payOperationRequest запрос на выполнение операции оплаты
     * @return данные страницы для банковского платежа
     */
    override fun cardPayOperation(payOperationRequest: CardPayOperationRequest): BankPaymentPageData =
        payOperationRequest
            .checkAvailability()
            .cardPayOperationCommand()
            .runCommand()

    /**
     * Формирует команду и стратегию по регистрации и получению платежной страницы в банке ГПБ для регистрации карты.
     *
     * @param payOperationRequest запрос на выполнение операции регистрации карты
     * @return данные страницы для банковского платежа
     */
    override fun regPayOperation(payOperationRequest: PayRegOperationRequest): BankPaymentPageData =
        payOperationRequest
            .checkAvailability()
            .regPayOperationCommand()
            .runCommand()

    /**
     * Формирует объект команды для запроса.
     * Вызывает функцию формирования стратегии в контексте того же запроса
     */
    private fun CardPayOperationRequest.cardPayOperationCommand() =
        gpbOperationCommand(
            requestToOperationMapper = idempotentOrderOperationMapper::toIdempotentOrderOperation,
            strategy = cardPayStrategy(),
        )

    /**
     * Формирует объект команды для запроса.
     * Вызывает функцию формирования стратегии в контексте того же запроса
     */
    private fun PayRegOperationRequest.regPayOperationCommand() =
        gpbOperationCommand(
            requestToOperationMapper = idempotentOrderOperationMapper::toIdempotentOrderOperation,
            strategy = regPayStrategy(),
        )

    /**
     * Вызывается относительно определенного запроса на оплату картой.
     * Формирует стратегию банковской операции по оплате картой относительно этого запроса.
     */
    private fun CardPayOperationRequest.cardPayStrategy() =
        stepWithSave(
            action = gpbCardIntegration::authorize,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByAuthorizedTrx,
        ).stepWithSave(
            action = gpbCardIntegration::cardPay,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankPaymentPage,
        )

    /**
     * Вызывается относительно определенного запроса на регистрацию картой.
     * Формирует стратегию банковской операции по регистрации карты относительно этого запроса.
     */
    private fun PayRegOperationRequest.regPayStrategy() =
        stepWithSave(
            action = gpbCardIntegration::authorize,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByAuthorizedTrx,
        ).stepWithSave(
            action = gpbCardIntegration::regPay,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankPaymentPage,
        )

    /**
     * Формирует команду и стратегию по регистрации и получению платежной страницы в банке ГПБ.
     *
     * @param payOperationRequest запрос на выполнение операции оплаты
     * @return данные страницы для банковского платежа
     */
    override fun sbpPayOperation(payOperationRequest: SbpPayOperationRequest): BankPaymentPageData =
        payOperationRequest
            .checkAvailability()
            .sbpPayOperationCommand()
            .runCommand()

    /**
     * Формирует объект команды для запроса.
     * Вызывает функцию формирования стратегии в контексте того же запроса
     */
    private fun SbpPayOperationRequest.sbpPayOperationCommand() =
        gpbOperationCommand(
            requestToOperationMapper = idempotentOrderOperationMapper::toIdempotentOrderOperation,
            strategy = sbpPayStrategy(),
        )

    /**
     * Вызывается относительно определенного запроса на оплату по сбп.
     * Формирует стратегию банковской операции по оплате по сбп относительно этого запроса.
     */
    private fun SbpPayOperationRequest.sbpPayStrategy() =
        stepWithSave(
            action = gpbSbpIntegration::sbpPay,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankPaymentPage,
        )

    /**
     * Формирует команду и стратегию для выполнения рекуррентного платежа картой в банке.
     *
     * @param recurrentOperationRequest запрос на выполнение операции оплаты
     * @return детали выполненного рекуррентного платежа
     */
    override fun recurrentOperation(recurrentOperationRequest: CardRecurrentOperationRequest): BankOperationDetails =
        recurrentOperationRequest
            .checkAvailability(OperationBank.GPB)
            .cardRecurrentPayOperationCommand()
            .runCommand()

    /**
     * Формирует объект команды для запроса.
     * Вызывает функцию формирования стратегии в контексте того же запроса.
     * Добавляет план действий при возбуждении ошибки и при финальном статусе операции.
     */
    private fun CardRecurrentOperationRequest.cardRecurrentPayOperationCommand() =
        gpbOperationCommand(
            requestToOperationMapper = idempotentOrderOperationMapper::toIdempotentOrderOperation,
            strategy = cardRecurrentPayStrategy(),
        ) onFailure {
            operationDetailsProducer.sendFailureOperationDetails(this, RECURRENT_INTERNAL_ERROR)
        } onFinalState {
            operationDetailsProducer.sendOperationDetails(this, it)
        }

    /**
     * Вызывается относительно определенного запроса на рекуррентную оплату картой.
     * Формирует стратегию банковской операции по рекуррентной оплате картой относительно этого запроса.
     */
    private fun CardRecurrentOperationRequest.cardRecurrentPayStrategy() =
        stepWithSave(
            action = gpbCardIntegration::authorize,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByAuthorizedTrx,
        ).stepWithSave(
            action = gpbCardIntegration::recurrentPay,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankOperationDetails,
        )

    /**
     * Формирует команду и стратегию для регистрации платежной страницы в банке ГПБ
     * и последующем получении qr кода с этой страницы.
     *
     * @param payOperationRequest запрос на выполнение операции оплаты
     * @return данные qr кода для банковского платежа
     */
    override fun qrImageSbpPayOperation(payOperationRequest: SbpPayOperationRequest): BankPaymentQrContent =
        payOperationRequest
            .checkAvailability()
            .qrImageSbpPayOperationCommand()
            .runCommand()

    /**
     * Формирует объект команды для запроса.
     * Вызывает функцию формирования стратегии в контексте того же запроса
     */
    private fun SbpPayOperationRequest.qrImageSbpPayOperationCommand() =
        gpbOperationCommand(
            requestToOperationMapper = idempotentOrderOperationMapper::toIdempotentOrderOperation,
            strategy = qrImageSbpPayStrategy(),
        )

    /**
     * Вызывается относительно определенного запроса на получение qr кода для оплаты по сбп.
     * Формирует стратегию банковской операции по получению qr кода для оплаты по сбп относительно этого запроса.
     */
    private fun SbpPayOperationRequest.qrImageSbpPayStrategy() =
        stepWithSave(
            action = gpbSbpIntegration::sbpPay,
            resultToOrderOperationMapper = idempotentOrderOperationMapper::updateByBankPaymentPage,
        ).step(
            action = gpbSbpIntegration::getQrContent,
        )

    /**
     * Проверяет доступность операции по правилу.
     */
    private fun PayOperationRequest.checkAvailability(bank: OperationBank? = null): PayOperationRequest {
        val available = rulePaymentTypeService.isOperationAvailable(operationType, paymentType, bank)
        if (available) return this

        logger.warn(
            "Операция недоступна по правилу. operationType [{}], paymentType [{}], bank [{}], orderId [{}]",
            operationType,
            paymentType,
            bank,
            orderId,
        )
        throw InnerException(getTraceId(), OPERATION_NOT_AVAILABLE_ERROR)
    }

    /**
     * Общая функция для запуска выполнения команды в сервисе операций
     */
    private fun <REQUEST : PayOperationRequest, RESULT> OperationCommand<REQUEST, RESULT>.runCommand(): RESULT =
        operationService.runOperation(this)
}
