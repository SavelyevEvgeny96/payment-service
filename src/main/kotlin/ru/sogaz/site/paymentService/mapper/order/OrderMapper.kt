package ru.sogaz.site.paymentService.mapper.order

import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValuePropertyMappingStrategy
import ru.sogaz.site.paymentService.dto.data.ClientCardDetails
import ru.sogaz.site.paymentService.dto.data.DataOrderPaymentPageInfo
import ru.sogaz.site.paymentService.dto.data.PaidOrderMessage
import ru.sogaz.site.paymentService.dto.data.PaySbp
import ru.sogaz.site.paymentService.dto.data.SubOrderPayload
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.enums.OrderStatus
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Mapper(imports = [OrderStatus::class])
interface OrderMapper {
    companion object {
        @JvmStatic
        @Named("instantToLocalDateTime")
        fun instantToLocalDateTime(paymentEndDate: Instant): LocalDateTime = LocalDateTime.ofInstant(paymentEndDate, ZoneId.systemDefault())

        @JvmStatic
        @Named("localDateTimeToFormattedString")
        fun localDateTimeToFormattedString(dateTime: LocalDateTime): String =
            dateTime
                .atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(
        target = "status",
        expression = "java(updatePaymentInvoiceRequest.getCancelCheck() ? OrderStatus.CANCELED : order.getStatus())",
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

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "externalSystemCode", source = "order.clientId")
    @Mapping(
        target = "paySuccess",
        source = "order.updateDate",
        qualifiedByName = ["localDateTimeToFormattedString"],
    )
    @Mapping(target = "subOrders", source = "subOrderPayloads")
    @Mapping(target = "keyCard", source = "cardDetails.cardId")
    fun toPaidOrderMessage(
        order: Order,
        subOrderPayloads: List<SubOrderPayload>,
        cardDetails: ClientCardDetails?,
    ): PaidOrderMessage

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "accounts", source = "order.subOrders")
    fun toOrderPaymentPageInfo(
        order: Order,
        paySbp: PaySbp?,
        urlPayBank: URI,
    ): DataOrderPaymentPageInfo
}
