package ru.sogaz.site.paymentService.service.v2.pay.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.mapper.v2.order.IdempotentOrderOperationMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentQrContent
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardRecurrentOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
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

@Service
class PayOperationServiceImpl(
    private val operationService: OperationService,
    private val gpbCardIntegration: GpbCardIntegration,
    private val gpbSbpIntegration: GpbSbpPayIntegration,
    private val idempotentOrderOperationMapper: IdempotentOrderOperationMapper,
    private val operationDetailsProducer: OperationDetailsProducer,
) : PayOperationService {
    companion object {
        private const val RECURRENT_INTERNAL_ERROR = "Платежная система недоступна"
    }

    /**
     * Формирует команду и стратегию по регистрации и получению платежной страницы в банке ГПБ.
     *
     * @param payOperationRequest запрос на выполнение операции оплаты
     * @return данные страницы для банковского платежа
     */
    override fun cardPayOperation(payOperationRequest: CardPayOperationRequest): BankPaymentPageData =
        payOperationRequest
            .cardPayOperationCommand()
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
     * Формирует команду и стратегию по регистрации и получению платежной страницы в банке ГПБ.
     *
     * @param payOperationRequest запрос на выполнение операции оплаты
     * @return данные страницы для банковского платежа
     */
    override fun sbpPayOperation(payOperationRequest: SbpPayOperationRequest): BankPaymentPageData =
        payOperationRequest
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
     * Общая функция для запуска выполнения команды в сервисе операций
     */
    private fun <REQUEST : PayOperationRequest, RESULT> OperationCommand<REQUEST, RESULT>.runCommand(): RESULT =
        operationService.runOperation(this)
}
