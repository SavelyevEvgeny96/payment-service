package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.BeanMapping
import org.mapstruct.Builder
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.NullValuePropertyMappingStrategy
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.dto.rabbit.PaymentCreatedEventDto
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.WaitingPayment

@Mapper(
    componentModel = "spring",
    builder = Builder(disableBuilder = true),
)
interface PaymentMapper {
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun toWaitingPayment(payment: Payment): WaitingPayment

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun toCallbackPayment(payment: Payment): CallbackPayment

    @Mapping(source = "data.bank", target = "bank")
    @Mapping(source = "data.paymentType", target = "type")
    @Mapping(target = "paymentStarted", ignore = true)
    @Mapping(target = "paymentFinished", ignore = true)
    fun toPayment(event: PaymentCreatedEventDto): Payment

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "state", constant = "NEW"),
        Mapping(target = "order", source = "order"),
        Mapping(target = "bank", source = "payload.bank"),
        Mapping(target = "type", expression = "java(PaymentTypeEnum.CARD)"),
        Mapping(target = "keyCard", source = "payload.keyCard"),
        Mapping(target = "saveCard", source = "order.saveCard"),
        Mapping(target = "urlToReturn", ignore = true),
        Mapping(target = "paymentStarted", ignore = true),
        Mapping(target = "paymentFinished", ignore = true),
    )
    fun orderToPayment(
        order: Order,
        payload: OrderPayloadDto,
    ): Payment
}
