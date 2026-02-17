package ru.sogaz.site.paymentService.service.rabbit.impl

import com.rabbitmq.client.Channel
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import ru.sogaz.site.paymentService.dao.PaymentDao
import ru.sogaz.site.paymentService.dto.data.ParsedResult
import ru.sogaz.site.paymentService.dto.data.PayloadInfo
import ru.sogaz.site.paymentService.dto.data.PayloadInfoExtractor
import ru.sogaz.site.paymentService.dto.data.RefundPayloadDto
import ru.sogaz.site.paymentService.dto.response.StatusRefundResponseDto
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentExtendedCodeMessage
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.BankIntegrationService
import ru.sogaz.site.paymentService.service.ReceiptService
import ru.sogaz.site.paymentService.service.rabbit.RefundPaymentConsumer
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer
import ru.sogaz.site.paymentService.service.rabbit.impl.RecurringPaymentConsumerImpl.Companion.NOT_VALID_BATCH_MESSAGE_ORDER_CREATED
import java.time.LocalDate
import java.util.UUID

/**
 * RabbitMQ consumer для обработки сообщений на возврат платежа.
 *
 * Поток обработки:
 * 1) Парсинг входящего сообщения и извлечение orderId
 * 2) Проверка существования платежа и его состояния
 * 3) Проверка срока валидности возврата (24 часа с момента завершения платежа)
 * 4) Вызов банковской интеграции для регистрации возврата
 * 5) Отправка статуса возврата в обменник
 *
 * При ошибке парсинга сообщение отправляется в поток "raw" (parking lot / DLQ-like),
 * чтобы не терять данные и иметь возможность дальнейшего разбора.
 */
@Service
class RefundPaymentConsumerImpl(
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProperties,
    private val paymentDao: PaymentDao,
    @Qualifier("GPBankIntegrationServiceImpl")
    private val bankIntegrationService: BankIntegrationService,
    private val receiptService: ReceiptService,
) : RefundPaymentConsumer {
    private val logger = loggerFor(RefundPaymentConsumerImpl::class.java)

    /**
     * Экстрактор orderId из сырого тела сообщения.
     *
     * Вынесен в поле, чтобы не создавать новый объект на каждый входящий message.
     */
    private val orderIdExtractor: PayloadInfoExtractor =
        PayloadInfoExtractor { body ->
            sendMessageProducer
                .extractOrderIdUnsafe(body)
                ?.let { PayloadInfo.OrderId(it) }
        }

    /**
     * Точка входа Rabbit listener-а.
     *
     * @param messages AMQP сообщение
     * @param channel RabbitMQ channel (используется для confirm/ack/nack в зависимых методах)
     */
    @RabbitListener(queues = ["\${app.rabbit.payment-refund-queue}"])
    override fun handleMessage(
        messages: Message,
        channel: Channel,
    ) {
        val parsedResult =
            sendMessageProducer.parseMessage(
                messages,
                channel,
                RefundPayloadDto::class.java,
                orderIdExtractor,
            )

        // Если результат невалидный или пустой — просто логируем и выходим
        if (parsedResult == null) {
            logger.warn(NOT_VALID_BATCH_MESSAGE_ORDER_CREATED)
            // Ничего полезного не нашли → реджект
            try {
                channel.basicReject(messages.messageProperties.deliveryTag, false)
            } catch (ackEx: Exception) {
                logger.error("Не удалось сделать basicReject для tag=${messages.messageProperties.deliveryTag}", ackEx)
            }
            return
        }
        when (parsedResult) {
            is ParsedResult.Success -> handleSuccess(parsedResult)
            is ParsedResult.Error -> handleParseError(parsedResult, channel)
        }
    }

    /**
     * Обработка успешно распарсенного сообщения на возврат.
     *
     * @param parsed результат парсинга с DTO
     */
    private fun handleSuccess(parsed: ParsedResult.Success<RefundPayloadDto>) {
        val dto = parsed.dto
        val orderId = dto.orderId

        val payment =
            paymentDao.findLastPaymentByOrderId(orderId!!).orElse(null)
                ?: return sendRefundError(
                    dto = dto,
                    orderId = orderId,
                    error = PaymentExtendedCodeMessage.PAYMENT_DATA_NOT_FOUND,
                )

        if (payment.bank != BankEnum.GPB) {
            return sendRefundError(
                dto = dto,
                orderId = orderId,
                error = PaymentExtendedCodeMessage.PAYMENT_NOT_FOUND_IS_NOT_GPB,
            )
        } else {
            if (payment.state != PaymentStatusEnum.SUCCESS) {
                return sendRefundError(
                    dto = dto,
                    orderId = orderId,
                    error = PaymentExtendedCodeMessage.PAYMENT_STATUS_NOT_SUCCESS,
                )
            }

            val finishedAt =
                payment.paymentFinished
                    ?: return sendRefundError(
                        dto = dto,
                        orderId = orderId,
                        error = PaymentExtendedCodeMessage.PAYMENT_EXPIRED,
                    )

            if (LocalDate.now().isEqual(finishedAt.toLocalDate())) {
                return sendRefundError(
                    dto = dto,
                    orderId = orderId,
                    error = PaymentExtendedCodeMessage.PAYMENT_EXPIRED,
                )
            }

            runCatching {
                bankIntegrationService.registerRefundForThePayment(payment, dto)
            }.onFailure { ex ->
                logger.error(
                    "Ошибка при регистрации возврата в банке. orderId=$orderId, paymentBankId=${payment.paymentBankId}",
                    ex,
                )
                sendRefundError(
                    dto = dto,
                    orderId = orderId,
                    error = PaymentExtendedCodeMessage.PAYMENT_SYSTEM_IS_NOT_AVAILABLE,
                )
            }.onSuccess { bankResp ->
                val status = bankResp.status
                if (status == StatusEnum.UNKNOWN.value || status == StatusEnum.FAILED.value) {
                    sendRefundError(
                        dto = dto,
                        orderId = orderId,
                        error = PaymentExtendedCodeMessage.OPERATION_ERROR_ON_THE_BANK,
                    )
                } else {
                    payment.state = PaymentStatusEnum.REFUND
                    paymentDao.save(payment)
                    receiptService.generateReceipt(payment)
                }
            }
        }
    }

    /**
     * Обработка ошибки парсинга.
     *
     * Логирует проблему и отправляет raw payload в отдельный маршрут,
     * чтобы сообщение не терялось и могло быть повторно обработано/проанализировано.
     *
     * @param parsed результат парсинга с ошибкой
     * @param channel RabbitMQ channel
     */
    private fun handleParseError(
        parsed: ParsedResult.Error,
        channel: Channel,
    ) {
        logger.error(
            "Ошибка парсинга. Автор: ${parsed.payloadInfo}, тело: ${parsed.rawBody}, TAG:${parsed.tag}",
        )

        sendMessageProducer.sendRawMessageWithConfirm(
            channel = channel,
            exchange = props.exchangePayment,
            routingKey = props.routingKeyPaymentStatusRefund,
            rawBody = parsed.rawBody,
        )
    }

    /**
     * Отправляет успешный статус возврата.
     *
     * @param dto входной payload возврата
     * @param orderId идентификатор заказа
     */
    private fun sendRefundSuccess(
        dto: RefundPayloadDto,
        orderId: UUID?,
    ) {
        val response =
            StatusRefundResponseDto(
                dto.metaInfo,
                orderId,
                StatusEnum.SUCCESS.value,
                null,
            )

        sendMessageProducer.sendMessage(
            props.routingKeyPaymentStatusRefund,
            response,
            props.exchangePayment,
            orderId.toString(),
        )
    }

    /**
     * Отправляет ошибочный статус возврата.
     *
     * @param dto входной payload возврата
     * @param orderId идентификатор заказа (может быть null, но для корректной маршрутизации лучше иметь ключ)
     * @param error бизнес-код/сообщение ошибки
     */
    private fun sendRefundError(
        dto: RefundPayloadDto,
        orderId: UUID?,
        error: PaymentExtendedCodeMessage,
    ) {
        val response =
            StatusRefundResponseDto(
                dto.metaInfo,
                orderId,
                StatusEnum.ERROR.value,
                error.message,
            )

        sendMessageProducer.sendMessage(
            props.routingKeyPaymentStatusRefund,
            response,
            props.exchangePayment,
            orderId.toString(),
        )
    }
}
