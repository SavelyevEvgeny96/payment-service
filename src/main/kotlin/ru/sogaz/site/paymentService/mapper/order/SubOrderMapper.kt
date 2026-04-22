package ru.sogaz.site.paymentService.mapper.order

import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValuePropertyMappingStrategy
import ru.sogaz.site.paymentService.dto.request.SubOrderPayload
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.SubOrder
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Mapper
interface SubOrderMapper {
    companion object {
        @JvmStatic
        @Named("instantToFormattedString")
        fun instantToFormattedString(instant: Instant?): String? =
            instant
                ?.atOffset(ZoneOffset.UTC)
                ?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun updateSubOrder(
        updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest,
        @MappingTarget subOrder: SubOrder,
    ): SubOrder

    // убрал policyDate и contractDate мапинг в виде даты и вернули в виде числа для подписок
    fun toSubOrderPayload(subOrder: SubOrder): SubOrderPayload

    fun toPayloadList(src: List<SubOrder>): List<SubOrderPayload>
}
