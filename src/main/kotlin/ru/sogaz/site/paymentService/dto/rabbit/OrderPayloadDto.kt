package ru.sogaz.site.paymentService.dto.rabbit

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.paymentService.dto.request.SubOrderRequest
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.validation.constraint.Email
import ru.sogaz.site.paymentService.validation.constraint.UniqueMainContract
import java.time.Instant
import java.util.UUID

data class OrderPayloadDto(
    @field:NotNull
    val metaInfo: List<MetaInfoOrder>,
    @field:NotBlank
    var subscriptionId: String, // id подписки
    @field:Email
    var recipientEmail: String?, // email страхователя
    @field:NotNull
    var bank: BankEnum? = null,
    @field:NotBlank
    var paymentType: String?,
    var recipientPhone: String?, // телефон страхователя
    var recipientUserId: String?, // ID личного кабинета
    var keyCard: String? = null, // ключ карты
    var orderEndDate: Instant?, // срок актуальности заказа
    var unifiedId: String? = null, // золотой ID
    var policyholder: String? = null,
    @field:UniqueMainContract
    var subOrders: List<SubOrderRequest>, // список полисов внутри заказа
    @JsonProperty("order_id_recurrent")
    var orderIdRecurrent: UUID? = null, // новое необязательное поле
)
