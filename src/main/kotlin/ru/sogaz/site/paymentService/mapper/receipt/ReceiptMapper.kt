package ru.sogaz.site.paymentService.mapper.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest.ReceiptTypeEnum
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest.SystemEnum
import ru.sogaz.site.payment.receipt.client.model.PaymentReceiptCreateRequest.VersionEnum
import ru.sogaz.site.paymentService.entity.Payment

@Mapper(
    uses = [ReceiptTotalAmountMapper::class, ReceiptItemMapper::class, ReceiptPaymentMapper::class, ReceiptClientInfoMapper::class],
    imports = [ReceiptTypeEnum::class, SystemEnum::class, VersionEnum::class],
)
interface ReceiptMapper {
    @Mapping(target = "paymentId", source = ".", qualifiedByName = ["mapPaymentId"])
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "total", source = "order.premiumAmount", qualifiedByName = ["mapToBigDecimalAmount"])
    @Mapping(target = "client", source = "order")
    @Mapping(target = "items", source = "order.subOrders")
    @Mapping(target = "payments", source = ".")
    @Mapping(target = "receiptType", source = "state")
    @Mapping(target = "system", constant = "ATOL")
    @Mapping(target = "version", constant = "V4")
    fun mapFromPaymentToReceiptCreateRequest(payment: Payment): PaymentReceiptCreateRequest
}
