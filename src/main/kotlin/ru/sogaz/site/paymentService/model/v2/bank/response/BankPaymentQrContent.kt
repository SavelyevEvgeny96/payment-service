package ru.sogaz.site.paymentService.model.v2.bank.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.sbp.QrImageData

data class BankPaymentQrContent(
    @field:Schema(example = "GPB")
    val bank: BankEnum,
    @field:Schema(example = "AD1000KGBS8LH9RH94K268UHORPOPUHX")
    val paymentBankId: String,
    @field:Schema(example = "https://qr.nspk.ru/AD1000KGBS8LH9RH94K268UHORPOPUHX?type=02&bank=100000000001&sum=1000&cur=RUB&crc=KHGN")
    val paymentPageUrl: String,
    val qrImageData: QrImageData,
)
