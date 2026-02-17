package ru.sogaz.site.paymentService.mapper.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.payment.receipt.client.model.PaymentPaymentRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentPaymentRequest.TypeEnum
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentService.entity.Payment
import ru.sogaz.site.paymentService.enums.PaymentStatusEnum

@Mapper(
    uses = [ReceiptTotalAmountMapper::class],
    imports = [TypeEnum::class],
)
abstract class ReceiptPaymentMapper {
    companion object {
        private const val INCORRECT_PAYMENT_STATE = "Некорректный статус платежа"
    }

    @Mapping(target = "type", constant = "_1")
    @Mapping(target = "sum", source = "order.premiumAmount", qualifiedByName = ["mapToBigDecimalAmount"])
    abstract fun mapFromPaymentToPaymentRequest(payment: Payment): PaymentPaymentRequest

    fun mapFromPaymentToListOfPaymentRequests(payment: Payment): List<PaymentPaymentRequest> =
        listOf(mapFromPaymentToPaymentRequest(payment))

    fun mapPaymentType(paymentStatus: PaymentStatusEnum): PaymentReceiptCreateRequest.ReceiptTypeEnum =
        when (paymentStatus) {
            PaymentStatusEnum.SUCCESS -> PaymentReceiptCreateRequest.ReceiptTypeEnum.SELL
            PaymentStatusEnum.REFUND -> PaymentReceiptCreateRequest.ReceiptTypeEnum.SELL_REFUND
            else -> throw InnerException(getTraceId(), INCORRECT_PAYMENT_STATE)
        }
}
