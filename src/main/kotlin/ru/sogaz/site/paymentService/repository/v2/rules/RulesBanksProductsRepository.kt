package ru.sogaz.site.paymentService.repository.v2.rules

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentService.model.v2.entity.rules.RulesBanksProducts
import ru.sogaz.site.paymentService.model.v2.enums.PaymentType
import java.util.UUID

interface RulesBanksProductsRepository : JpaRepository<RulesBanksProducts, UUID> {
    fun findFirstByInsuranceKindAndPaymentTypeAndActiveTrueOrderByUpdateDateDesc(
        insuranceKind: String,
        paymentType: PaymentType,
    ): RulesBanksProducts?
}
