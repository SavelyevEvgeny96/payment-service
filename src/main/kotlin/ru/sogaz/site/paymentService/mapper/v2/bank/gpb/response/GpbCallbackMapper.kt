package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.common.GpbPayStatusMapper
import ru.sogaz.site.paymentService.model.v2.bank.callback.GpbCardCallback
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.enums.OperationState

@Mapper(
    uses = [GpbCardDetailMapper::class, GpbPayStatusMapper::class],
    imports = [OperationState::class],
)
interface GpbCallbackMapper {
    @Mapping(target = "state", source = ".", defaultValue = "WAIT")
    @Mapping(target = "bankId", source = "trx_id")
    @Mapping(target = "cardDetails", source = ".")
    @Mapping(target = "errorText", source = "extResultCode.message")
    @Mapping(target = "operationFinished", expression = "java( Instant.now() )")
    fun toBankOperationDetails(gpbCallback: GpbCardCallback): BankOperationDetails
}
