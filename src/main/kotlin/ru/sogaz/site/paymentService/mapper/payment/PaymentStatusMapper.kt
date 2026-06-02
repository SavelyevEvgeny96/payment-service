package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import ru.sogaz.site.paymentService.dto.request.GpbCallback
import ru.sogaz.site.paymentService.enums.AbrPaymentStatusEnum
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum
import ru.sogaz.site.paymentService.enums.StatusEnum

@Mapper
abstract class PaymentStatusMapper {
    fun convert(gpbCallback: GpbCallback): PaymentStatusEnum =
        when (gpbCallback.result_code) {
            1 -> PaymentStatusEnum.SUCCESS
            else -> PaymentStatusEnum.WAIT
        }

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

            StatusEnum.REFUND,
            -> PaymentStatusEnum.REFUND

            else -> PaymentStatusEnum.WAIT
        }

    fun convert(abrStatus: AbrPaymentStatusEnum): PaymentStatusEnum =
        when (abrStatus) {
            AbrPaymentStatusEnum.PARTPAID,
            AbrPaymentStatusEnum.REFUNDED,
            AbrPaymentStatusEnum.VOIDED,
            -> PaymentStatusEnum.REFUND

            AbrPaymentStatusEnum.DECLINED,
            AbrPaymentStatusEnum.EXPIRED,
            -> PaymentStatusEnum.FAIL

            AbrPaymentStatusEnum.REFUSED -> PaymentStatusEnum.DECLINED
            AbrPaymentStatusEnum.FULLYPAID -> PaymentStatusEnum.SUCCESS
            else -> PaymentStatusEnum.WAIT
        }
}
