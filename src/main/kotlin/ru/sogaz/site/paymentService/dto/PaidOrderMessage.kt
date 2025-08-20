package ru.sogaz.site.paymentService.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaidOrderMessage(
    val orderId: String?,
    val recipientEmail: String?,
    val externalSystemCode: String,
    val docType: String?,
    val policyId: String?,
    val policyNumber: String?,
    val contractNumber: String?,
    val contractId: String?,
    val typeInsurance: String?,
    val premiumAmount: String?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") val paySuccess: String?,
) : Serializable
