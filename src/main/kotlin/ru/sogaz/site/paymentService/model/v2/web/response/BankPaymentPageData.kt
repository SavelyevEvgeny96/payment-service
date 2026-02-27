package ru.sogaz.site.paymentService.model.v2.web.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.enums.BankEnum

data class BankPaymentPageData(
    @field:Schema(example = "GPB")
    val bank: BankEnum,
    @field:Schema(example = "A100KB47RXYKIKSTYC7H")
    val paymentBankId: String,
    @field:Schema(example = "https://lt.pga.gazprombank.ru/pages#A100KB47RXYKIKSTYC7H")
    val paymentPageUrl: String,
)
