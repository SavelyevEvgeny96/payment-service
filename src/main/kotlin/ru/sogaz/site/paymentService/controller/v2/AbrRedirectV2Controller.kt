package ru.sogaz.site.paymentService.controller.v2

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.paymentService.api.doc.v2.AbrRedirectV2Api
import ru.sogaz.site.paymentService.service.v2.bank.abr.redirect.AbrRedirectAddressService

@RestController
@Tag(name = "ABR redirect v2", description = "Редирект клиента после оплаты в АБР")
class AbrRedirectV2Controller(
    private val abrRedirectAddressService: AbrRedirectAddressService,
) : AbrRedirectV2Api {
    override fun redirectByAbrState(
        id: String,
        status: String?,
    ): RedirectView = abrRedirectAddressService.getRedirectView(id, status)
}
