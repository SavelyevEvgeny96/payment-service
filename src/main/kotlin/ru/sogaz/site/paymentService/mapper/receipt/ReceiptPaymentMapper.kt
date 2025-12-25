package ru.sogaz.site.paymentService.mapper.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.payment.receipt.client.model.PaymentPaymentRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentPaymentRequest.TypeEnum
import ru.sogaz.site.paymentService.entity.Payment

@Mapper(
    uses = [ReceiptTotalAmountMapper::class],
    imports = [TypeEnum::class],
)
abstract class ReceiptPaymentMapper {
    @Mapping(target = "type", constant = "_1")
    @Mapping(target = "sum", source = "order.premiumAmount", qualifiedByName = ["mapToBigDecimalAmount"])
    abstract fun mapFromPaymentToPaymentRequest(payment: Payment): PaymentPaymentRequest

    fun mapFromPaymentToListOfPaymentRequests(payment: Payment): List<PaymentPaymentRequest> =
        listOf(mapFromPaymentToPaymentRequest(payment))
}
