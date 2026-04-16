package ru.sogaz.site.paymentService.mapper.v2.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.AuthorizedCardTrxData
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation
import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.CardPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.PayRegOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.pay.SbpPayOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.request.refund.RefundOperationRequest
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

/**
 * Интерфейс маппера для создания и обновления записей о банковских операциях в базе
 */
@Mapper
interface IdempotentOrderOperationMapper {
    @Mapping(target = "id", source = "orderId")
    fun toIdempotentOrder(operationRequest: OperationRequest): IdempotentOrder

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "premiumAmount", source = "amount")
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "operationStarted", expression = "java( Instant.now() )")
    fun toIdempotentOrderOperation(operationRequest: OperationRequest): IdempotentOrderOperation

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "premiumAmount", source = "amount")
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "operationStarted", expression = "java( Instant.now() )")
    fun toIdempotentOrderOperation(operationRequest: PayOperationRequest): IdempotentOrderOperation

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "premiumAmount", source = "amount")
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "operationStarted", expression = "java( Instant.now() )")
    fun toIdempotentOrderOperation(operationRequest: CardPayOperationRequest): IdempotentOrderOperation

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "premiumAmount", source = "amount")
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "operationStarted", expression = "java( Instant.now() )")
    fun toIdempotentOrderOperation(operationRequest: SbpPayOperationRequest): IdempotentOrderOperation

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "premiumAmount", source = "amount")
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "operationStarted", expression = "java( Instant.now() )")
    fun toIdempotentOrderOperation(operationRequest: PayRegOperationRequest): IdempotentOrderOperation

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "premiumAmount", source = "amount")
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "operationStarted", expression = "java( Instant.now() )")
    fun toIdempotentOrderOperation(operationRequest: RefundOperationRequest): IdempotentOrderOperation

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentBankId", source = "token")
    @Mapping(target = "state", constant = "REG")
    @Mapping(target = "operationStarted", expression = "java( Instant.now() )")
    fun updateByAuthorizedTrx(
        @MappingTarget idempotentOrderOperation: IdempotentOrderOperation,
        authorizedCardTrxData: AuthorizedCardTrxData,
    ): IdempotentOrderOperation

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "state", constant = "REG")
    @Mapping(target = "paymentBankUrl", source = "paymentPageUrl")
    @Mapping(target = "operationStarted", expression = "java( Instant.now() )")
    fun updateByBankPaymentPage(
        @MappingTarget idempotentOrderOperation: IdempotentOrderOperation,
        bankPaymentPageData: BankPaymentPageData,
    ): IdempotentOrderOperation

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "operationFinished", expression = "java( Instant.now() )")
    fun updateByBankOperationDetails(
        @MappingTarget idempotentOrderOperation: IdempotentOrderOperation,
        bankOperationDetails: BankOperationDetails,
    ): IdempotentOrderOperation
}
