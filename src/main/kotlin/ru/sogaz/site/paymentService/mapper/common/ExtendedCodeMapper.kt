package ru.sogaz.site.paymentService.mapper.common

import org.mapstruct.Named
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentService.enums.PaymentExtendedCodeMessage

@Component
class ExtendedCodeMapper {
    @Named("mapExtendedCode")
    fun mapExtendedCode(code: String?): String? {
        if (code.isNullOrBlank() || code == "OK") return null
        return PaymentExtendedCodeMessage.fromCode(code)
    }
}
