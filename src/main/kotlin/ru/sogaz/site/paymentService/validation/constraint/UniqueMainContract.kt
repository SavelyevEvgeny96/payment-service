package ru.sogaz.site.paymentService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.sogaz.site.paymentService.dto.request.OrderRequest
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueMainContractValidator::class])
annotation class UniqueMainContract(
    val message: String = "Not unique main contract",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class UniqueMainContractValidator : ConstraintValidator<UniqueMainContract, List<OrderRequest>> {
    override fun isValid(
        orders: List<OrderRequest>,
        context: ConstraintValidatorContext?,
    ): Boolean = orders.count { it.mainContractCheck } <= 1
}
