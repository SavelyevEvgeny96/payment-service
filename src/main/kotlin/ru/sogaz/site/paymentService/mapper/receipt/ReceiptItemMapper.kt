package ru.sogaz.site.paymentService.mapper.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.payment.receipt.client.model.PaymentItemRequest
import ru.sogaz.site.payment.receipt.client.model.PaymentItemRequest.PaymentMethodEnum
import ru.sogaz.site.paymentService.entity.SubOrder
import ru.sogaz.site.paymentService.enums.PaymentObjectEnum

@Mapper(
    uses = [ReceiptTotalAmountMapper::class],
    imports = [PaymentMethodEnum::class, PaymentObjectEnum::class],
)
interface ReceiptItemMapper {
    companion object {
        private const val RECEIPT_CONTRACT_NUMBER = "Страховая премия по договору №"

        @JvmStatic
        @Named("mapItemName")
        fun mapItemName(subOrder: SubOrder): String = "$RECEIPT_CONTRACT_NUMBER${subOrder.contractNumber}"
    }

    @Mapping(target = "name", source = ".", qualifiedByName = ["mapItemName"])
    @Mapping(target = "sum", source = "premiumAmount", qualifiedByName = ["mapToBigDecimalAmount"])
    @Mapping(target = "price", source = "premiumAmount", qualifiedByName = ["mapToBigDecimalAmount"])
    @Mapping(target = "paymentObject", expression = "java( PaymentObjectEnum.PAYMENT_OBJECT_SERVICE.getValue() )")
    @Mapping(target = "paymentMethod", constant = "FULL_PAYMENT")
    @Mapping(target = "quantity", constant = "1.0")
    @Mapping(target = "vatType", constant = "NONE")
    fun fromSubOrder(subOrder: SubOrder): PaymentItemRequest
}
