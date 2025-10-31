package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.dto.data.ClientCardDetails
import ru.sogaz.site.paymentService.dto.response.bank.GpbCardDetails

@Mapper
interface CardDetailsMapper {
    @Mapping(source = "pan", target = "maskedPan")
    @Mapping(source = "type", target = "paymentType")
    fun fromGpbSrcBankResponse(gpbCardDetails: GpbCardDetails): ClientCardDetails
}
