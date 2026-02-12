package ru.sogaz.site.paymentService.mapper.v2.bank.gpb

import org.mapstruct.Mapper
import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbCardPayStatus
import ru.sogaz.site.paymentService.model.v2.enums.OperationState

@Mapper
abstract class GpbPayStatusMapper {
    fun convertToOperationState(gpbStatus: GpbCardPayStatus): OperationState =
        when (gpbStatus) {
            GpbCardPayStatus.NEW,
            -> OperationState.NEW

            GpbCardPayStatus.BLOCKED,
            GpbCardPayStatus.REJECTED,
            GpbCardPayStatus.FAILED,
            -> OperationState.FAIL

            GpbCardPayStatus.DECLINED,
            -> OperationState.DECLINED

            GpbCardPayStatus.SUCCESS,
            GpbCardPayStatus.ACCEPTED,
            -> OperationState.SUCCESS

            GpbCardPayStatus.REFUND,
            -> OperationState.REFUND

            else -> OperationState.WAIT
        }
}
