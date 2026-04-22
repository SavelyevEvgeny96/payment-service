package ru.sogaz.site.paymentService.mapper.v2.bank.abr.common

import org.mapstruct.Mapper
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrPaymentStatus
import ru.sogaz.site.paymentService.model.v2.enums.OperationState

@Mapper
abstract class AbrPayStatusMapper {
    fun convertToOperationState(status: AbrPaymentStatus): OperationState =
        when (status) {
            AbrPaymentStatus.PREPARING,
            AbrPaymentStatus.WAITPUSHTRAN,
            AbrPaymentStatus.AUTHORIZED,
            -> OperationState.WAIT

            AbrPaymentStatus.PARTPAID,
            AbrPaymentStatus.FULLYPAID,
            -> OperationState.SUCCESS

            AbrPaymentStatus.REFUNDED,
            -> OperationState.REFUND

            AbrPaymentStatus.VOIDED,
            AbrPaymentStatus.DECLINED,
            AbrPaymentStatus.EXPIRED,
            AbrPaymentStatus.REFUSED,
            -> OperationState.FAIL

            AbrPaymentStatus.CLOSED,
            -> OperationState.WAIT
        }
}
