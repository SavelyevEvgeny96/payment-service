package ru.sogaz.site.paymentService.config.feign

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

class GpbSbpClientConfig {

    companion object {
        private const val TEST_PROFILE = "test"

        private const val QRC_DATA_PATH = "merchant/qrc-data"

        private const val PAYMENT_SERVICE_ID_HEADER = "paymentServiceId"
        private const val PAYMENT_SERVICE_ID_VALUE = "PS0000000001"
    }

    @Bean
    @Profile(TEST_PROFILE)
    fun gpbSbpPaymentServiceIdInterceptor(): RequestInterceptor =
        RequestInterceptor { template ->
            val isQrcDataRequest = template.path()
                .trimStart('/')
                .startsWith(QRC_DATA_PATH)

            if (isQrcDataRequest) {
                template.header(
                    PAYMENT_SERVICE_ID_HEADER,
                    PAYMENT_SERVICE_ID_VALUE,
                )
            }
        }
}