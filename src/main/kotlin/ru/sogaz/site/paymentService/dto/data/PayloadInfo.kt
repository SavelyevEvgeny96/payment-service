package ru.sogaz.site.paymentService.dto.data

sealed class PayloadInfo {
    data class Author(val value: String) : PayloadInfo()
    data class OrderId(val value: String) : PayloadInfo()
}