package ru.sogaz.site.paymentService.properties

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "ssl.client")
data class SslClientProperties(
    @field:NotBlank var pfxBase64: String? = null,
    @field:NotBlank var pfxPassword: String? = null,
    @field:NotBlank var caBase64: String? = null,

    var tlsProtocols: List<String> = listOf("TLSv1.2", "TLSv1.3"),
    var hostnameVerification: Boolean = true,

    @field:NotNull var connectTimeoutMs: Int = 5000,
    @field:NotNull var readTimeoutMs: Int = 35000,

    var pool: Pool = Pool()
) {
    data class Pool(
        var maxTotal: Int = 200,
        var defaultMaxPerRoute: Int = 50
    )
}