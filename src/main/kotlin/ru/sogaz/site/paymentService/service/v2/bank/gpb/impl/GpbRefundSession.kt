package ru.sogaz.site.paymentService.service.v2.bank.gpb.impl

import ru.sogaz.site.paymentService.clients.gpb.GpbCardRefundClient
import ru.sogaz.site.paymentService.model.v2.bank.properties.gpb.GpbCardAccountData
import java.io.Closeable

class GpbRefundSession(
    private val gpbCardRefundClient: GpbCardRefundClient,
    val account: GpbCardAccountData,
) : Closeable {
    companion object {
        private const val SESSION_TOKEN_PREFIX = "Session "
    }

    var sessionToken = SESSION_TOKEN_PREFIX

    fun initSession(): GpbRefundSession {
        val sessionId =
            gpbCardRefundClient
                .startSession(
                    portalId = account.portalId,
                    identifier = account.identifier,
                    password = account.password,
                ).sessionId
        sessionToken = "$SESSION_TOKEN_PREFIX$sessionId"
        return this
    }

    override fun close() {
        gpbCardRefundClient.finishSession(sessionToken, account.portalId)
    }
}
