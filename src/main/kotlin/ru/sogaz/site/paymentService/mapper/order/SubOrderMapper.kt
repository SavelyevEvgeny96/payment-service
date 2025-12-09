package ru.sogaz.site.paymentService.mapper.order

import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy
import ru.sogaz.site.paymentService.dto.request.SubOrderPayload
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.SubOrder

@Mapper
interface SubOrderMapper {
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun updateSubOrder(
        updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest,
        @MappingTarget subOrder: SubOrder,
    ): SubOrder

    fun toSubOrderPayload(subOrder: SubOrder): SubOrderPayload

    fun toPayloadList(src: List<SubOrder>): List<SubOrderPayload>
}
