package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import ru.sogaz.site.paymentService.dto.response.AkbOrderStatusResponse
import ru.sogaz.site.paymentService.dto.response.bank.GpbSbpPaymentStatusResponse
import ru.sogaz.site.paymentService.enums.AkbPaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.StatusEnum

@Mapper
abstract class PaymentStatusMapper {
    fun convert(gpbStatus: StatusEnum): PaymentStatusEnum =
        when (gpbStatus) {
            StatusEnum.NEW,
            -> PaymentStatusEnum.NEW

            StatusEnum.BLOCKED,
            StatusEnum.REJECTED,
            StatusEnum.FAILED,
            -> PaymentStatusEnum.FAIL

            StatusEnum.DECLINED,
            -> PaymentStatusEnum.DECLINED

            StatusEnum.SUCCESS,
            StatusEnum.ACCEPTED,
            -> PaymentStatusEnum.SUCCESS

            else -> PaymentStatusEnum.WAIT
        }

    fun convert(akbStatus: AkbPaymentStatusEnum): PaymentStatusEnum =
        when (akbStatus) {
            AkbPaymentStatusEnum.PARTPAID,
            AkbPaymentStatusEnum.REFUNDED,
            AkbPaymentStatusEnum.VOIDED,
            -> PaymentStatusEnum.REFUND

            AkbPaymentStatusEnum.DECLINED,
            AkbPaymentStatusEnum.EXPIRED,
            -> PaymentStatusEnum.FAIL

            AkbPaymentStatusEnum.REFUSED -> PaymentStatusEnum.DECLINED
            AkbPaymentStatusEnum.FULLYPAID -> PaymentStatusEnum.SUCCESS
            else -> PaymentStatusEnum.WAIT
        }

    fun convert(gpbSbpPaymentStatusResponse: GpbSbpPaymentStatusResponse): PaymentStatusEnum =
        gpbSbpPaymentStatusResponse.result
            .firstOrNull()
            ?.status
            ?.run(::convert)
            ?: PaymentStatusEnum.WAIT

    fun convert(akbOrderStatusResponse: AkbOrderStatusResponse): PaymentStatusEnum {
        if (akbOrderStatusResponse.status == AkbPaymentStatusEnum.CLOSED) {
            return when (akbOrderStatusResponse.prevStatus) {
                AkbPaymentStatusEnum.PREPARING,
                AkbPaymentStatusEnum.WAITPUSHTRAN,
                AkbPaymentStatusEnum.AUTHORIZED,
                -> return PaymentStatusEnum.WAIT
                else -> PaymentStatusEnum.FAIL
            }
        }
        return convert(akbOrderStatusResponse.status)
    }
}
