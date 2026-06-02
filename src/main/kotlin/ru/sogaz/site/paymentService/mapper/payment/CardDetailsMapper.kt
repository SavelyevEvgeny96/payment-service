package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.dto.data.ClientCardDetails
import ru.sogaz.site.paymentService.dto.request.GpbCallback
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardDetails

@Mapper
interface CardDetailsMapper {
    @Mapping(source = "pan", target = "maskedPan")
    @Mapping(source = "type", target = "paymentType")
    fun fromGpbSrcBankResponse(gpbCardDetails: GpbCardDetails): ClientCardDetails

    @Mapping(target = "maskedPan", source = "p.maskedPan")
    @Mapping(target = "paymentSystem", source = "p.paymentSystem")
    @Mapping(target = "issuerName", source = "p.issuerName")
    @Mapping(target = "cardId", source = "card.id")
    fun fromGpbCallback(gpbCallback: GpbCallback): ClientCardDetails
}
