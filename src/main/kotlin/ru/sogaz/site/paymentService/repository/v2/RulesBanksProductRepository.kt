package ru.sogaz.site.paymentService.repository.v2

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentService.model.v2.entity.RulesBanksProduct
import java.util.UUID

@Repository
interface RulesBanksProductRepository : JpaRepository<RulesBanksProduct, UUID> {
    fun findFirstByInsuranceKindAndPaymentTypeAndActiveTrue(
        insuranceKind: String,
        paymentType: String,
    ): RulesBanksProduct?
}
