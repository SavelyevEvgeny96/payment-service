package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentService.mapper.v2.bank.gpb.common.GpbPayStatusMapper
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.BankPaymentQrContent
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbQrImageResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpPayResponse
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.GpbSbpResult
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import ru.sogaz.site.paymentService.model.v2.web.response.BankPaymentPageData

@Mapper(
    uses = [GpbPayStatusMapper::class],
    imports = [OperationState::class],
)
interface GpbSbpResponseMapper {
    @Mapping(target = "paymentBankId", source = "data.qrcId")
    @Mapping(target = "paymentPageUrl", source = "data.payload")
    @Mapping(target = "bank", constant = "GPB")
    fun toBankPaymentPageData(response: GpbSbpPayResponse): BankPaymentPageData

    @Mapping(target = "state", source = "status", defaultValue = "WAIT")
    @Mapping(target = "bankId", source = "id")
    fun toBankOperationDetails(response: GpbSbpResult): BankOperationDetails

    @Mapping(target = "qrImageData", source = "gpbQrImageResponse.data.image")
    fun toBankPaymentQrData(
        bankPaymentPageData: BankPaymentPageData,
        gpbQrImageResponse: GpbQrImageResponse,
    ): BankPaymentQrContent
}
