// WebConfigRestTemplate.kt
package ru.sogaz.site.paymentService.config

import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory
import org.apache.hc.client5.http.ssl.TrustAllStrategy
import org.apache.hc.core5.ssl.SSLContexts
import org.apache.hc.core5.util.Timeout
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.paymentService.properties.SslClientProperties
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext

@Configuration
@EnableConfigurationProperties(SslClientProperties::class)
class WebConfigRestTemplate {
    /**
     * Обычный RestTemplate (без сертификатов)
     */
    @Bean
    fun defaultRestTemplate(): RestTemplate = RestTemplate()

    /**
     * Специальный RestTemplate с PFX + CA из base64
     */
    @Bean
    fun xpgRestTemplate(props: SslClientProperties): RestTemplate {
        val sslContext = buildSslContext(props)
        val hostnameVerifier =
            if (props.hostnameVerification) DefaultHostnameVerifier() else NoopHostnameVerifier.INSTANCE

        val sslSocketFactory =
            SSLConnectionSocketFactory(
                sslContext,
                props.tlsProtocols.toTypedArray(),
                null,
                hostnameVerifier,
            )

        val cm: PoolingHttpClientConnectionManager =
            PoolingHttpClientConnectionManagerBuilder
                .create()
                .setSSLSocketFactory(sslSocketFactory)
                .build()
                .apply {
                    maxTotal = props.pool.maxTotal
                    defaultMaxPerRoute = props.pool.defaultMaxPerRoute
                }

        val requestConfig =
            RequestConfig
                .custom()
                .setConnectTimeout(Timeout.ofMilliseconds(props.connectTimeoutMs.toLong()))
                .setResponseTimeout(Timeout.ofMilliseconds(props.readTimeoutMs.toLong()))
                .build()

        val httpClient: CloseableHttpClient =
            HttpClients
                .custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy())
                .build()

        val factory =
            HttpComponentsClientHttpRequestFactory(httpClient).apply {
                setConnectTimeout((props.connectTimeoutMs))
            }

        return RestTemplate(factory)
    }

    // Доработать не корректно собирается trustStore(может быть дело в CA сертификате)
    private fun buildSslContext(props: SslClientProperties): SSLContext {
        val keyStore = loadPkcs12FromBase64(props.pfxBase64!!, props.pfxPassword!!.toCharArray())
        // val trustStore = buildTrustStoreFromBase64(props.caBase64!!)
        return SSLContexts
            .custom()
            .loadKeyMaterial(keyStore, props.pfxPassword!!.toCharArray())
            // .loadTrustMaterial(trustStore, null)
            .loadTrustMaterial(null, TrustAllStrategy.INSTANCE) // доверяет любой цепочке использую пока его
            .build()
    }

    private fun loadPkcs12FromBase64(
        b64: String,
        password: CharArray,
    ): KeyStore {
        val decoded = base64Decode(b64)
        val ks = KeyStore.getInstance("PKCS12")
        ByteArrayInputStream(decoded).use { ks.load(it, password) }
        return ks
    }

    private fun buildTrustStoreFromBase64(b64: String): KeyStore {
        val trust = KeyStore.getInstance(KeyStore.getDefaultType())
        trust.load(null, null)

        val bytes = base64Decode(b64)
        val cf = CertificateFactory.getInstance("X.509")
        val certs = parseCertificates(bytes, cf)

        certs.forEachIndexed { i, cert ->
            trust.setCertificateEntry("ca-$i", cert)
        }
        return trust
    }

    private fun parseCertificates(
        bytes: ByteArray,
        cf: CertificateFactory,
    ): List<X509Certificate> {
        val text =
            try {
                String(bytes, Charsets.UTF_8)
            } catch (_: Exception) {
                ""
            }
        if (text.contains("-----BEGIN CERTIFICATE-----")) {
            val regex = Regex("-----BEGIN CERTIFICATE-----([\\s\\S]+?)-----END CERTIFICATE-----")
            val matches = regex.findAll(text).toList()
            if (matches.isNotEmpty()) {
                return matches.map {
                    val innerB64 = it.groupValues[1]
                    val der = Base64.getMimeDecoder().decode(innerB64)
                    cf.generateCertificate(ByteArrayInputStream(der)) as X509Certificate
                }
            }
        }
        return try {
            cf.generateCertificates(ByteArrayInputStream(bytes)).map { it as X509Certificate }
        } catch (_: Exception) {
            listOf(cf.generateCertificate(ByteArrayInputStream(bytes)) as X509Certificate)
        }
    }

    private fun base64Decode(b64: String): ByteArray =
        try {
            Base64.getDecoder().decode(b64)
        } catch (_: IllegalArgumentException) {
            Base64.getMimeDecoder().decode(b64)
        }
}
