package ru.sogaz.site.paymentService.mapper.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.payment.receipt.client.model.ClientInfo
import ru.sogaz.site.paymentService.entity.Order

@Mapper
interface ReceiptClientInfoMapper {
    @Mapping(target = "email", source = "recipientEmail")
    @Mapping(target = "phone", source = "recipientPhone")
    @Mapping(target = "name", source = "recipientUserId")
    fun fromOrder(order: Order): ClientInfo
}
