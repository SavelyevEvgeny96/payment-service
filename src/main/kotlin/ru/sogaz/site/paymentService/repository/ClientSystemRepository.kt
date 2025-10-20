package ru.sogaz.site.paymentService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.entity.ClientSystem

@Repository
interface ClientSystemRepository : JpaRepository<ClientSystem, Long> {
    fun findByExternalSystemCode(externalSystemCode: String?): ClientSystem?
}
