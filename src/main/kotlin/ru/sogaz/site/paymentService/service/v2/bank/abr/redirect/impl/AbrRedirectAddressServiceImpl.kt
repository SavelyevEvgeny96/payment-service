package ru.sogaz.site.paymentService.service.v2.bank.abr.redirect.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.enums.AbrPaymentStatusEnum
import ru.sogaz.site.paymentService.model.v2.entity.redirect.RedirectAddresses
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams
import ru.sogaz.site.paymentService.properties.ApiConfigProperties
import ru.sogaz.site.paymentService.repository.v2.redirect.RedirectAddressesRepository
import ru.sogaz.site.paymentService.service.v2.bank.abr.redirect.AbrRedirectAddressService
import java.util.UUID

@Service
class AbrRedirectAddressServiceImpl(
    private val redirectAddressesRepository: RedirectAddressesRepository,
    private val apiConfigProperties: ApiConfigProperties,
) : AbrRedirectAddressService {
    companion object {
        private val unsuccessfulPaymentStatuses =
            setOf(
                AbrPaymentStatusEnum.DECLINED,
                AbrPaymentStatusEnum.REFUSED,
                AbrPaymentStatusEnum.EXPIRED,
            )
    }

    override fun createStateRedirectUrl(redirectParams: RedirectParams): String =
        redirectParams
            .toRedirectAddresses()
            .run(redirectAddressesRepository::save)
            .id
            .let(::requireNotNull)
            .run(::buildStateRedirectUrl)

    override fun getRedirectView(
        id: String,
        status: String?,
    ): RedirectView =
        (
            id
                .toUuidOrNull()
                ?.let(redirectAddressesRepository::findById)
                ?.orElse(null)
                ?.resolveRedirectUrl(status)
                ?: apiConfigProperties.returnUrl
        ).toFoundRedirectView()

    private fun RedirectParams.toRedirectAddresses() =
        RedirectAddresses(
            id = null,
            urlToReturnS = urlToReturnS,
            urlToReturnF = urlToReturnF,
            urlToReturn = urlToReturn,
            createDate = null,
        )

    private fun buildStateRedirectUrl(id: UUID): String = "${apiConfigProperties.abrStateRedirectUrl.trimEnd('/')}/$id"

    private fun RedirectAddresses.resolveRedirectUrl(status: String?): String =
        when {
            status.isAbrStatus(AbrPaymentStatusEnum.FULLYPAID) -> urlToReturnS ?: urlToReturn ?: apiConfigProperties.returnUrl
            unsuccessfulPaymentStatuses.any { status.isAbrStatus(it) } -> urlToReturnF ?: urlToReturn ?: apiConfigProperties.returnUrl
            else -> urlToReturn ?: apiConfigProperties.returnUrl
        }

    private fun String?.isAbrStatus(status: AbrPaymentStatusEnum): Boolean = this == status.value || this == status.name

    private fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()

    private fun String.toFoundRedirectView(): RedirectView = RedirectView(this).apply { HttpStatus.FOUND }
}
