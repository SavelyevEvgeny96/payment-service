package ru.sogaz.site.paymentService.dto.data

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class QueueMessageDto(
    val correlationId: String = UUID.randomUUID().toString(),
    val flowCode: String = "ResultPay",
    val messageId: String = UUID.randomUUID().toString(),
    val description: String = "Process_ResultPay",
    val variables: List<VariableDto>,
) : Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VariableDto(
    val name: String,
    val value: Any,
) : Serializable
