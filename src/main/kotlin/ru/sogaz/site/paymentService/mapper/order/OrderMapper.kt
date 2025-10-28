package ru.sogaz.site.paymentService.mapper.order

import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValuePropertyMappingStrategy
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.data.PaidOrderMessage
import ru.sogaz.site.paymentService.dto.data.SubOrderPayload
import ru.sogaz.site.paymentService.dto.request.UpdatePaymentInvoiceRequest
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.enums.OrderStatus
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
    @Mapping(target = "recipientEmail", source = "order.recipientEmail")
    @Mapping(target = "externalSystemCode", source = "order.clientId")
    @Mapping(target = "subscriptionId", source = "order.subscriptionId")
    @Mapping(
        target = "paySuccess",
        source = "order.updateDate",
        qualifiedByName = ["localDateTimeToFormattedString"],
    )
    @Mapping(target = "subOrders", source = "subOrderPayloads")
    @Mapping(target = "issuerName", source = "bankPaymentDetails.cardDetails.issuerName")
    @Mapping(target = "paymentType", source = "bankPaymentDetails.cardDetails.paymentType")
    @Mapping(target = "maskedPan", source = "bankPaymentDetails.cardDetails.maskedPan")
    @Mapping(target = "paymentSystem", source = "bankPaymentDetails.cardDetails.paymentSystem")
    fun toPaidOrderMessage(
        order: Order,
        subOrderPayloads: List<SubOrderPayload>,
        bankPaymentDetails: BankPaymentDetails,
    ): PaidOrderMessage
}

// val orderId: String?,
// val recipientEmail: String?,
// val externalSystemCode: String? = null,
// val subscriptionId: String?,
// val paySuccess: String?,
// val subOrders: List<SubOrderPayload>?,
// val issuerName: String?,
// val paymentType: String?,
// val maskedPan: String?,
// val paymentSystem: String?,

// orderId = order.id?.toString(),
// recipientEmail = order.recipientEmail,
// externalSystemCode = order.clientId,
// subscriptionId = order.subscriptionId,
// paySuccess =
// order.updateDate
// ?.atZone(ZoneOffset.UTC)
// ?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
// subOrders = subOrderPayloads,
// bankPaymentDetails.cardDetails?.issuerName,
// bankPaymentDetails.cardDetails?.paymentSystem,
// bankPaymentDetails.cardDetails?.maskedPan,
// bankPaymentDetails.cardDetails?.paymentType,
