package ru.sogaz.site.paymentService.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sogaz.site.paymentService.enums.BankEnum
import ru.sogaz.site.paymentService.validation.constraint.Email
import ru.sogaz.site.paymentService.validation.constraint.UniqueMainContract
import java.time.Instant

/**
 * @property orders Лист данных по отдельному payments(каждый payments имеет отдельную запись в таблице subOrder)
 * @property orderEndDate Дата и время окончания действия ссылки на оплату (обязательное поле)
 * @property recipientEmail Электронная почта получателя (обязательное поле)
 * @property bank Банк для совершения операции (необязательное поле, если не указан — используется дефолтный банк из конфигурации)
 * @property urlToReturn URL для перехода после успешной оплаты (обязательное поле)
 * @property urlToDecline URL для перехода после неуспешной оплаты (обязательное поле)

 */
data class OrderRequest(
    @get:Valid
    @field:UniqueMainContract(message = "{validation.orderPaymentRequest.uniqueMainContract}")
    val orders: List<SubOrderRequest> = emptyList(),
    val bank: BankEnum? = null,
    @field:NotNull(message = "{validation.orderPaymentRequest.date.notNull}")
    @field:Future(message = "{validation.orderPaymentRequest.date.future}")
    val orderEndDate: Instant? = null,
    @field:NotBlank(message = "{validation.orderRequest.notBlank}")
    @field:Email(message = "{validation.orderPaymentRequest.recipientEmail.email}")
    val recipientEmail: String = "",
    val urlToReturn: String? = null,
    val urlToDecline: String? = null,
    val saveCard: Boolean = false,
)
