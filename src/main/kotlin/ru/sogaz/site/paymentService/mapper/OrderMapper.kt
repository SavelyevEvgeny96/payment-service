package ru.sogaz.site.paymentService.mapper

import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValuePropertyMappingStrategy
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.enums.OrderStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Objects

@Mapper(imports = [OrderStatus::class, Objects::class])
interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(
        target = "status",
        expression =
            "java(!Objects.isNull(updatePaymentInvoiceRequest.getCancelCheck())" +
                " && updatePaymentInvoiceRequest.getCancelCheck()" +
                " ? OrderStatus.CANCELED : order.getStatus())",
    )
    @Mapping(
        target = "paymentEndDate",
        source = "paymentEndDate",
        qualifiedByName = ["instantToLocalDateTime"],
    )
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun updateOrder(
        updatePaymentInvoiceRequest: UpdatePaymentInvoiceRequest,
        @MappingTarget order: Order,
    ): Order

    companion object {
        @JvmStatic
        @Named("instantToLocalDateTime")
        fun instantToLocalDateTime(paymentEndDate: Instant): LocalDateTime = LocalDateTime.ofInstant(paymentEndDate, ZoneId.systemDefault())
    }
}
