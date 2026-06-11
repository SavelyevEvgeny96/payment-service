package ru.sogaz.site.paymentService.api.doc.v2

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.view.RedirectView

interface AbrRedirectV2Api {
    @GetMapping("/v2/payment/abr/state/{id}")
    fun redirectByAbrState(
        @PathVariable id: String,
        @RequestParam("STATUS", required = false) status: String?,
    ): RedirectView
}
