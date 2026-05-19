package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.sbp.GpbSbpReversalConfirmRequest
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.sbp.GpbSbpReversalPrepareRequest
import ru.sogaz.site.paymentService.model.v2.web.request.refund.RefundOperationRequest
import ru.sogaz.site.paymentService.properties.gpb.GpbSbpAccountProperties
import java.math.BigDecimal

@Mapper
abstract class GpbSbpReversalRequestMapper {
    @Mapping(target = "transactionId", source = "request.paymentBankId")
    @Mapping(target = "amount", source = "request.amount", qualifiedByName = ["mapAmount"])
    @Mapping(target = "currency", constant = "RUB")
    @Mapping(target = "comment", source = "request.description")
    @Mapping(target = "callbackMerchantNotifications", source = "accountProperties.account.callbackUrlSbp")
    abstract fun toPrepareRequest(request: RefundOperationRequest, accountProperties: GpbSbpAccountProperties): GpbSbpReversalPrepareRequest

    @Mapping(target = "transactionId", source = "prepareTransactionId")
    abstract fun toConfirmRequest(prepareTransactionId: String): GpbSbpReversalConfirmRequest

    fun toHeaders(accountProperties: GpbSbpAccountProperties): HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("login", accountProperties.reversal.login)
            set("password", accountProperties.reversal.password)
        }

    @Named("mapAmount")
    fun mapAmount(amount: BigDecimal): Int = amount.movePointRight(2).intValueExact()
}
