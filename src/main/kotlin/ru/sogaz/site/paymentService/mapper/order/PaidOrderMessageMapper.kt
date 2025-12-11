package ru.sogaz.site.paymentService.mapper.order

import org.mapstruct.Builder
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.Named
import ru.sogaz.site.paymentService.dto.data.PaymentRecurrentRegisterData
import ru.sogaz.site.paymentService.dto.request.PaidOrderMessage
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Mapper(
    componentModel = "spring",
    builder = Builder(disableBuilder = true),
    imports = [PaymentStatusEnum::class],
    uses = [SubOrderMapper::class],
)
interface PaidOrderMessageMapper {
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

    @Mappings(
        // ----- данные из Order внутри Payment -----
        Mapping(target = "orderId", source = "payment.order.orderIdRecurrent"),
        Mapping(target = "recipientEmail", source = "payment.order.recipientEmail"),
        Mapping(target = "externalSystemCode", source = "payment.order.clientId"),
        Mapping(target = "subscriptionId", source = "payment.order.subscriptionId"),
        // ----- SUCCESS / ERROR -----
        Mapping(
            target = "paySuccess",
            source = "payment.paymentFinished",
            qualifiedByName = ["localDateTimeToFormattedString"],
        ),
        // ----- саб-ордера -----
        // order.subOrders: List<SubOrder> -> List<SubOrderPayload>
        Mapping(target = "subOrders", source = "payment.order.subOrders"),
        // ----- данные по карте из ответа банка -----
        Mapping(target = "issuerName", source = "bankResponse.gpbCardDetails.issuerName"),
        Mapping(target = "paymentType", source = "payment.type"),
        Mapping(target = "maskedPan", source = "bankResponse.gpbCardDetails.pan"),
        Mapping(target = "paymentSystem", source = "bankResponse.gpbCardDetails.paymentSystem"),
        // ----- статус и банк -----
        Mapping(
            target = "status",
            expression =
                "java(src.getPayment().getState() == PaymentStatusEnum.REG ? \"success\" : \"error\")",
        ),
        Mapping(target = "keyCard", source = "payment.keyCard"),
        Mapping(target = "bank", source = "payment.bank"),
        // ----- ошибка в ошибочном кейсе -----
        Mapping(target = "errorText", source = "bankResponse.error"),
    )
    fun toPaidOrderMessage(src: PaymentRecurrentRegisterData): PaidOrderMessage

    fun toPaidOrderMessages(src: List<PaymentRecurrentRegisterData>): List<PaidOrderMessage>
}
