package ru.sogaz.site.paymentService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder.bind
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.config.converters.NoOpMessageConverter
import ru.sogaz.site.paymentService.properties.RabbitListenerProps
import ru.sogaz.site.paymentService.properties.RabbitProperties

@Configuration
class RabbitMQConfig(
    private val connectionFactory: ConnectionFactory,
    private val props: RabbitProperties,
    private val propsListener: RabbitListenerProps,
) {
    companion object {
        const val QUEUE_TYPE = "x-queue-type"
        const val QUORUM = "quorum"
    }

    @Bean(name = ["paymentsExchange"])
    fun paymentsExchange(): TopicExchange = TopicExchange(props.exchangeOrder, true, false)

    @Bean(name = ["ordersExchange"])
    fun ordersExchange(): TopicExchange = TopicExchange(props.exchangePayment, true, false)

    @Bean(name = ["orderStatusPaidQueue"])
    fun orderStatusPaidQueue(): Queue =
        QueueBuilder
            .durable(props.queueStatusOrderPaid)
            .withArgument(QUEUE_TYPE, QUORUM)
            .build()

    @Bean(name = ["orderStatusUnpaidQueue"])
    fun orderStatusUnpaidQueue(): Queue =
        QueueBuilder
            .durable(props.queueStatusOrderUnpaid)
            .withArgument(QUEUE_TYPE, QUORUM)
            .build()

    @Bean(name = ["paymentsStatusQueue"])
    fun paymentsStatusQueue(): Queue =
        QueueBuilder
            .durable(props.queueStatusPayment)
            .withArgument(QUEUE_TYPE, QUORUM)
            .build()

    @Bean
    fun paymentsStatusBinding(
        @Qualifier("paymentsStatusQueue") queue: Queue,
        @Qualifier("paymentsExchange") exchange: TopicExchange,
    ): Binding = bind(queue).to(exchange).with(props.routingKeyStatusPayment)

    @Bean
    fun orderStatusUnpaidBinding(
        @Qualifier("orderStatusUnpaid") queue: Queue,
        @Qualifier("ordersExchange") exchange: TopicExchange,
    ): Binding = bind(queue).to(exchange).with(props.routingKeyStatusOrderUnpaid)

    @Bean
    fun orderStatusPaidBinding(
        @Qualifier("orderStatusPaidQueue") queue: Queue,
        @Qualifier("ordersExchange") exchange: TopicExchange,
    ): Binding = bind(queue).to(exchange).with(props.routingKeyStatusOrderPaid)

    @Bean
    fun jacksonMessageConverter(objectMapper: ObjectMapper): MessageConverter =
        Jackson2JsonMessageConverter(objectMapper)

    @Bean
    fun rabbitTemplate(
        @Qualifier("jacksonMessageConverter") messageConverter: MessageConverter,
    ): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }

    @Bean("batchContainerFactory")
    fun batchContainerFactory(noOpMessageConverter: NoOpMessageConverter): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setBatchListener(true)
            setConsumerBatchEnabled(true)
            setDeBatchingEnabled(true)
            setBatchSize(propsListener.batchSize)
            setPrefetchCount(propsListener.prefetch)
            setConcurrentConsumers(propsListener.concurrency)
            setMaxConcurrentConsumers(propsListener.maxConcurrency)
            setAcknowledgeMode(AcknowledgeMode.MANUAL)
            setChannelTransacted(true)
            setDefaultRequeueRejected(false)
            setMessageConverter(noOpMessageConverter)
        }
}
