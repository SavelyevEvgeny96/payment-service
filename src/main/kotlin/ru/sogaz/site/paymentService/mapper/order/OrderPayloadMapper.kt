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
            "paymentType",
        ],
    )
    @Mapping(target = "orders", source = "subOrders")
    @Mapping(target = "urlToReturn", ignore = true)
    @Mapping(target = "urlToDecline", ignore = true)
    @Mapping(target = "saveCard", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    fun toRequest(payload: OrderPayloadDto): OrderRequest
}
