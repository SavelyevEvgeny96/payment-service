package ru.sogaz.site.paymentService.service.v2.bank.abr.redirect

import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.model.v2.web.request.common.RedirectParams

interface AbrRedirectAddressService {
    fun createStateRedirectUrl(redirectParams: RedirectParams): String

    fun getRedirectView(id: String, status: String?): RedirectView
}
