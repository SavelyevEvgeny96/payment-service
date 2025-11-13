package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValuePropertyMappingStrategy
import ru.sogaz.site.paymentService.dto.rabbit.PaymentCreatedEventDto
import ru.sogaz.site.paymentService.entity.CallbackPayment
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.entity.WaitingPayment

@Mapper
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
}
