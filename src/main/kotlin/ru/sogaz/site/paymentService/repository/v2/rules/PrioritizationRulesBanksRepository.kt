package ru.sogaz.site.paymentService.repository.v2.rules

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentService.model.v2.entity.rules.PrioritizationRulesBanks
import java.util.UUID

interface PrioritizationRulesBanksRepository : JpaRepository<PrioritizationRulesBanks, UUID> {
    fun findFirstByOrderByUpdateDateDesc(): PrioritizationRulesBanks?
}
