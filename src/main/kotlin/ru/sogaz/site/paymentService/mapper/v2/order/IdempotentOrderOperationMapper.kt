package ru.sogaz.site.paymentService.mapper.v2.order

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrder
import ru.sogaz.site.paymentService.model.v2.entity.IdempotentOrderOperation

@Mapper
interface IdempotentOrderOperationMapper {
    @Mapping(target = "id", ignore = true)
    fun toOrderPaymentOperation(
        idempotentOrder: IdempotentOrder,
        depersonalization: Boolean,
    ): IdempotentOrderOperation
}
