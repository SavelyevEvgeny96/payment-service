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
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.PaymentExtendedCodeMessage
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.BankIntegrationService
import ru.sogaz.site.paymentService.service.rabbit.RefundPaymentConsumer
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer
import java.time.LocalDateTime
import java.util.*

@Service
class RefundPaymentConsumerImpl(
    private val sendMessageProducer: SendMessageProducer,
    private val props: RabbitProperties,
    private val paymentDao: PaymentDao,
    @Qualifier("GPBankIntegrationServiceImpl")
    private val bankIntegrationService: BankIntegrationService,
) : RefundPaymentConsumer {
    private val logger = loggerFor(RefundPaymentConsumerImpl::class.java)

    @RabbitListener(
        queues = ["\${app.rabbit.payment-refund-queue}"],
    )
    override fun handleBatch(
        messages: Message,
        channel: Channel,
    ) {
        val orderIdExtractor =
            PayloadInfoExtractor { body ->
                sendMessageProducer.extractOrderIdUnsafe(body)
                    ?.let { PayloadInfo.OrderId(it) }
            }

        val parsedResults =
            sendMessageProducer.parseMessage(
                messages,
                channel,
                RefundPayloadDto::class.java,
                orderIdExtractor,
            ) ?: return

        when (parsedResults) {
            is ParsedResult.Success -> {
                handleSuccess(parsedResults)
            }

            is ParsedResult.Error -> {
                handleParseError(parsedResults, channel)
            }
        }
    }

    private fun handleSuccess(parsed: ParsedResult.Success<RefundPayloadDto>) {
        val dto = parsed.dto
        val orderId = dto.orderId
        val payment: Payment?
        val paymentOpt = paymentDao.findByPaymentOrderId(orderId)
        if (paymentOpt.isPresent) {
            payment = paymentOpt.get()
            if (payment.state != PaymentStatusEnum.SUCCESS) {
                sendRefundError(
                    dto,
                    orderId,
                    PaymentExtendedCodeMessage.PAYMENT_STATUS_NOT_SUCCESS,
                )
                return
            }
            val finishedAt = payment.paymentFinished
            val expiredAt = finishedAt?.plusHours(24)

            if (LocalDateTime.now().isAfter(expiredAt) || finishedAt == null) {
                sendRefundError(
                    dto,
                    orderId,
                    PaymentExtendedCodeMessage.PAYMENT_EXPIRED,
                )
                return
            }
            try {
                if (payment.bank == BankEnum.GPB) {
                    bankIntegrationService.registerRefundForThePayment(payment,dto)
                }
            } catch (ex: Exception) {
                sendRefundError(
                    dto,
                    orderId,
                    PaymentExtendedCodeMessage.PAYMENT_SYSTEM_IS_NOT_AVAILABLE,
                )
            }
        } else {
            sendRefundError(
                dto,
                orderId,
                PaymentExtendedCodeMessage.PAYMENT_DATA_NOT_FOUND,
            )
        }
    }

    private fun handleParseError(
        parsed: ParsedResult.Error,
        channel: Channel,
    ) {
        logger.error(
            "Ошибка парсинга. Автор: ${parsed.payloadInfo}, " +
                    "тело: ${parsed.rawBody}, TAG:${parsed.tag}",
        )

        sendMessageProducer.sendRawMessageWithConfirm(
            channel,
            props.exchangePayment,
            props.routingKeyPaymentStatusRefund,
            parsed.rawBody,
        )
    }

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
