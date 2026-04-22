package ru.sogaz.site.paymentService.mapper.v2.bank.abr.response

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.mapper.v2.bank.abr.common.AbrPayStatusMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrOrderResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrPaymentStatusResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrPaymentStatus.CLOSED
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrPaymentStatus.PREPARING
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrPaymentStatus.WAITPUSHTRAN
import ru.sogaz.site.paymentService.model.v2.bank.response.abr.AbrPaymentStatus.AUTHORIZED
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData
import java.time.Instant

@Mapper(uses = [AbrPayStatusMapper::class])
interface AbrCardResponseMapper {
    @Mapping(target = "paymentBankId", expression = "java( String.valueOf(response.getOrder().getId()) )")
    @Mapping(target = "paymentPageUrl", source = "order.hppUrl")
    @Mapping(target = "bank", constant = "ABR")
    fun toBankPaymentPageData(response: AbrOrderResponse): BankPaymentPageData

    @Mapping(target = "bankId", source = "order.id")
    @Mapping(target = "state", expression = "java( resolveCurrentStatus(response) )")
    @Mapping(target = "operationFinished", expression = "java( Instant.now() )")
    fun toBankOperationDetails(response: AbrPaymentStatusResponse): BankOperationDetails

    fun resolveCurrentStatus(response: AbrPaymentStatusResponse) =
        when {
            response.order.status != CLOSED -> response.order.status
            response.order.prevStatus in arrayOf(PREPARING, WAITPUSHTRAN, AUTHORIZED) -> PREPARING
            else -> CLOSED
        }
}
