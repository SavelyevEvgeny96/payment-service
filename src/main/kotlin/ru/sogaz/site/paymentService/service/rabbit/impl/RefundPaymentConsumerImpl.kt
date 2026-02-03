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
import ru.sogaz.site.paymentService.enums.StatusEnum
import ru.sogaz.site.paymentService.loggerFor
import ru.sogaz.site.paymentService.properties.RabbitProperties
import ru.sogaz.site.paymentService.service.BankIntegrationService
import ru.sogaz.site.paymentService.service.rabbit.RefundPaymentConsumer
import ru.sogaz.site.paymentService.service.rabbit.SendMessageProducer

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
                sendMessageProducer.extractOrderIdUnsafe(body)?.let { PayloadInfo.OrderId(it) }
            }
        // Парсинг сообщения messages->OrderPayloadDto
        val parsedResults =
            sendMessageProducer.parseMessage(
                messages,
                channel,
                RefundPayloadDto::class.java,
                orderIdExtractor,
            )
        if (parsedResults != null) {
            when (parsedResults) {
                is ParsedResult.Success -> {
                    val orderId = parsedResults.dto.orderId

                    val paymentOpt = paymentDao.findByPaymentOrderId(orderId)

                    if (paymentOpt.isPresent) {
                        val payment = paymentOpt.get()
                        if (payment.bank == BankEnum.GPB) {
                            bankIntegrationService.registerRefundForThePayment(payment)
                        }

                        // логика, если платеж найден
                    } else {
                        logger.info("Платеж не найден. orderId=$orderId")
                        val dtoNotPayment =
                            StatusRefundResponseDto(
                                parsedResults.dto.metaInfo,
                                orderId,
                                StatusEnum.ERROR.value,
                                PaymentExtendedCodeMessage.PAYMENT_DATA_NOT_FOUND.message,
                            )

                        sendMessageProducer.sendMessage(
                            props.routingKeyPaymentStatusRefund,
                            dtoNotPayment,
                            props.exchangePayment,
                            orderId.toString(),
                        )
                    }
                }

                is ParsedResult.Error -> {
                    logger.error(
                        "Ошибка парсинга. Автор: ${parsedResults.payloadInfo}, " +
                            "тело: ${parsedResults.rawBody}, TAG:${parsedResults.tag}",
                    )

                    sendMessageProducer.sendRawMessageWithConfirm(
                        channel,
                        props.exchangePayment,
                        props.routingKeyPaymentStatusRefund,
                        parsedResults.rawBody,
                    )
                }
            }
        }
    }
}
