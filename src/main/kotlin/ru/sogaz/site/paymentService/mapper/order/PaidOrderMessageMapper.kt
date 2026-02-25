package ru.sogaz.site.paymentService.mapper.order

import org.mapstruct.Builder
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import ru.sogaz.site.paymentService.dto.data.PaymentRecurrentRegisterData
import ru.sogaz.site.paymentService.dto.request.PaidOrderMessage
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.mapper.common.DateTimeMapper
import ru.sogaz.site.paymentService.mapper.common.ExtendedCodeMapper

@Mapper(
    componentModel = "spring",
    builder = Builder(disableBuilder = true),
    imports = [PaymentStatusEnum::class],
    uses = [
        SubOrderMapper::class,
        DateTimeMapper::class,
        ExtendedCodeMapper::class,
    ],
)
interface PaidOrderMessageMapper {
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
        // ----- ошибка (С ИСПОЛЬЗОВАНИЕМ enum → сообщение) -----
        Mapping(
            target = "errorText",
            source = "bankResponse.result.extendedCode",
            qualifiedByName = ["mapExtendedCode"],
        ),
    )
    @Mapping(target = "httpStatusCode", source = "bankResponse.httpStatusCode")
    fun toPaidOrderMessage(src: PaymentRecurrentRegisterData): PaidOrderMessage

    fun toPaidOrderMessages(src: List<PaymentRecurrentRegisterData>): List<PaidOrderMessage>
}
