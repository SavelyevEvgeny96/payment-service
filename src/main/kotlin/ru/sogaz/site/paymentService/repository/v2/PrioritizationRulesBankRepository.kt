package ru.sogaz.site.paymentService.repository.v2

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.model.v2.entity.PrioritizationRulesBank
import java.util.UUID

@Repository
interface PrioritizationRulesBankRepository : JpaRepository<PrioritizationRulesBank, UUID> {
    fun findFirstByOrderByUpdateDateDesc(): PrioritizationRulesBank?
}
