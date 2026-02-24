package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.model.v2.bank.response.ClientCardDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.GpbCardDetails

@Mapper
interface GpbCardDetailMapper {
    @Mapping(source = "pan", target = "maskedPan")
    @Mapping(source = "type", target = "paymentType")
    fun fromGpbSrcBankResponse(gpbCardDetails: GpbCardDetails): ClientCardDetails
}
