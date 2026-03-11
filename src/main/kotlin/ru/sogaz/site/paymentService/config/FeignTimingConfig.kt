package ru.sogaz.site.paymentService.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentService.loggerFor

class FeignTimingConfig {
    private val log = loggerFor(FeignTimingConfig::class.java)

    @Bean
    fun feignClient(delegate: feign.Client): feign.Client {
        return object : feign.Client {
            override fun execute(
                request: feign.Request,
                options: feign.Request.Options,
            ): feign.Response {
                val start = System.nanoTime()
                try {
                    log.info("FEIGN -> start {} {}", request.httpMethod(), request.url())

                    val resp = delegate.execute(request, options)
                    val durMs = (System.nanoTime() - start) / 1_000_000

                    log.info(
                        "FEIGN <- end  status={} durMs={} {} {}",
                        resp.status(),
                        durMs,
                        request.httpMethod(),
                        request.url(),
                    )
                    return resp
                } catch (e: Exception) {
                    val durMs = (System.nanoTime() - start) / 1_000_000
                    log.warn(
                        "FEIGN !! error  durMs={} {} {} ex={}",
                        durMs,
                        request.httpMethod(),
                        request.url(),
                        e::class.simpleName,
                        e,
                    )
                    throw e
                }
            }
        }
    }
}
