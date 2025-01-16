package ru.sogaz.site.paymentService.service
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import ru.sogaz.core.logger.LoggerFactory

/**
 * Репозиторий информации о приложении.
 */
@Component
@ConfigurationProperties(prefix = "app.info")
class AppInfoService {
    private val logger = LoggerFactory.getApiLogger(AppInfoService::class.java)

    lateinit var applicationName: String
    lateinit var artifactId: String
    lateinit var groupId: String
    lateinit var description: String
    lateinit var version: String
    lateinit var appProfile: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("applicationName = $applicationName")
        logger.info("description = $description")
        logger.info("version = $version")
        logger.info("artifactId = $artifactId")
        logger.info("groupId = $groupId")
        logger.info("appProfile = $appProfile")
    }
}
