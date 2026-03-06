package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.common

import org.mapstruct.Mapper
import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbCardPayStatus
import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbSbpPayStatus
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.GpbCardCallback
import ru.sogaz.site.paymentService.model.v2.enums.OperationState

@Mapper
abstract class GpbPayStatusMapper {
    fun convertToOperationState(gpbCallback: GpbCardCallback): OperationState =
        when (gpbCallback.result_code) {
            1 -> OperationState.SUCCESS
            2 -> OperationState.FAIL
            else -> OperationState.WAIT
        }

    fun convertToOperationState(gpbStatus: GpbSbpPayStatus): OperationState =
        when (gpbStatus) {
            GpbSbpPayStatus.NOT_STARTED -> OperationState.WAIT
            GpbSbpPayStatus.ACCEPTED -> OperationState.SUCCESS
            GpbSbpPayStatus.REJECTED -> OperationState.FAIL
        }

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
