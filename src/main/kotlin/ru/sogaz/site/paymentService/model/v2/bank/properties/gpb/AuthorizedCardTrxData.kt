package ru.sogaz.site.paymentService.model.v2.bank.properties.gpb

data class AuthorizedCardTrxData(
    val token: String,
    val account: GpbCardAccountData,
)
