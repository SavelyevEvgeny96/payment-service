package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.dto.data.BankPaymentDetails
import ru.sogaz.site.paymentService.dto.rabbit.MetaInfoOrder
import ru.sogaz.site.paymentService.dto.rabbit.StatusRegisterCardMessage
import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto
import ru.sogaz.site.paymentService.entity.Order
import ru.sogaz.site.paymentService.enums.CardRegisterStatus
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import java.time.Instant

@Mapper(
    componentModel = "spring",
)
abstract class RegisterCardMapper {
    abstract fun mapErrorBody(src: RegisterCardResponseDto): RegisterCardResponseDto

    @Mapping(
        target = "status",
        expression = "java(mapPaymentStatus(bankPaymentDetails.getStatus(), order.getRecurrent()))",
    )
    @Mapping(target = "errorText", source = "bankPaymentDetails.extendedCode")
    @Mapping(target = "keyCard", source = "bankPaymentDetails.cardDetails.cardId")
    @Mapping(target = "maskedPan", source = "bankPaymentDetails.cardDetails.maskedPan")
    @Mapping(target = "title", source = "bankPaymentDetails.cardDetails.title")
    @Mapping(target = "paymentSystem", source = "bankPaymentDetails.cardDetails.paymentSystem")
    @Mapping(target = "issuerName", source = "bankPaymentDetails.cardDetails.issuerName")
    @Mapping(target = "paymentType", source = "bankPaymentDetails.cardDetails.paymentType")
    @Mapping(target = "bank", source = "order.bank")
    @Mapping(target = "payDate", source = "timestamp")
    @Mapping(target = "unifiedId", source = "order.unifiedId")
    abstract fun toStatusRegisterCardMessage(
        metaInfo: MetaInfoOrder,
        order: Order,
        channel: String,
        bankPaymentDetails: BankPaymentDetails,
        timestamp: Instant,
    ): StatusRegisterCardMessage

    protected fun mapPaymentStatus(
        paymentStatus: PaymentStatusEnum,
        orderRecurrent: Boolean?,
    ): CardRegisterStatus? {
        val recurrent = orderRecurrent ?: false
        return when {
            paymentStatus == PaymentStatusEnum.SUCCESS -> CardRegisterStatus.SUCCESS

            recurrent &&
                paymentStatus in
                setOf(
                    PaymentStatusEnum.FAIL,
                    PaymentStatusEnum.REFUND,
                    PaymentStatusEnum.DECLINED,
                )
            -> CardRegisterStatus.ERROR

            else -> null
        }
    }
}
