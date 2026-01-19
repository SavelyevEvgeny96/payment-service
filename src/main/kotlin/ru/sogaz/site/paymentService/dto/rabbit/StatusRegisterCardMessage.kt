package ru.sogaz.site.paymentService.dto.rabbit

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.enums.CardRegisterStatus
import java.time.Instant
@JsonInclude(JsonInclude.Include.NON_NULL)
data class StatusRegisterCardMessage(
    val metaInfo: MetaInfoOrder,
    val channel: String,
    val unifiedId: String,
    val status: CardRegisterStatus?,
    val errorText: String?,
    val keyCard: String?,
    val maskedPan: String?,
    val title: String?,
    val paymentSystem: String?,
    val issuerName: String?,
    val paymentType: String?,
    val bank: BankEnum,
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss+00:00",
        timezone = "UTC",
    )
    val payDate: Instant,
)
