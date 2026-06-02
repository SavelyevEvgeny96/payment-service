package ru.sogaz.site.paymentService.mapper.order

import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.paymentService.dto.rabbit.MetaInfoOrder
import ru.sogaz.site.paymentService.dto.rabbit.OrderPayloadDto
import ru.sogaz.site.paymentService.dto.request.OrderRequest

@Mapper(componentModel = "spring")
abstract class OrderPayloadMapper {
    @BeanMapping(
        ignoreUnmappedSourceProperties = ["paymentType"],
    )
    @Mapping(target = "orders", source = "subOrders")
    @Mapping(target = "urlToReturn", ignore = true)
    @Mapping(target = "urlToDecline", ignore = true)
    @Mapping(target = "saveCard", ignore = true)
    @Mapping(target = "clientId", source = "metaInfo", qualifiedByName = ["mapClientId"])
    abstract fun toRequest(payload: OrderPayloadDto): OrderRequest

    @Named("mapClientId")
    open fun mapClientId(metaInfo: List<MetaInfoOrder>): String? = metaInfo.firstOrNull()?.author
}
