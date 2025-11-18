package ru.sogaz.site.paymentService.mapper.order

import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.dto.request.OrderRequest

@Mapper(componentModel = "spring")
interface OrderPayloadMapper {
    @BeanMapping(
        ignoreUnmappedSourceProperties = [
            "metaInfo",
            "bank",
            "paymentType",
            "keyCard",
        ],
    )
    @Mapping(target = "orders", source = "subOrders")
    @Mapping(target = "orderEndDate", source = "orderEndDate")
    @Mapping(target = "recipientEmail", source = "recipientEmail")
    @Mapping(target = "recipientUserId", source = "recipientUserId")
    @Mapping(target = "unifiedId", source = "unifiedId")
    @Mapping(target = "recipientPhone", source = "recipientPhone")
    @Mapping(target = "subscriptionId", source = "subscriptionId")
    @Mapping(target = "policyholder", source = "policyholder")
    @Mapping(target = "orderIdRecurrent", source = "orderIdRecurrent")
    @Mapping(target = "urlToReturn", ignore = true)
    @Mapping(target = "urlToDecline", ignore = true)
    @Mapping(target = "saveCard", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    fun toRequest(payload: OrderPayloadDto): OrderRequest
}
