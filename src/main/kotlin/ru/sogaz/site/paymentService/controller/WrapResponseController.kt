package ru.sogaz.site.paymentService.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.view.RedirectView
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentService.dto.data.UriKeeper
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse

abstract class WrapResponseController {
    protected fun <T> T.wrapToOkResponseEntity(): ResponseEntity<T> = ResponseEntity.ok(this)

    protected fun <T> T.wrapToSuccessResponse(statusCode: Int): Response<T> = getSuccessResponse(getTraceId(), statusCode, this)

    protected fun UriKeeper.wrapToRedirectView() = getUri().toString().run(::RedirectView)
}
