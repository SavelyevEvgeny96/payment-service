package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.request

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.model.v2.bank.request.gpb.gid.GpbGidStatusRequest
import ru.sogaz.site.paymentService.properties.gpb.GpbGidProperties

@Mapper
interface GpbGidStatusRequestMapper {
    @Mapping(target = "pgaTrxId", source = "paymentBankId")
    @Mapping(
        target = "portalId",
        expression =
            "java(depersonalization ? properties.getDepersonalizedPortalId() : properties.getPortalId())",
    )
    fun toGpbGidStatusRequest(
        paymentBankId: String,
        depersonalization: Boolean,
        properties: GpbGidProperties,
    ): GpbGidStatusRequest
}
