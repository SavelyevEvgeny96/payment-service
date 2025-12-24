package ru.sogaz.site.paymentService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import ru.sogaz.site.paymentService.dto.request.SubOrderRequest
import ru.sogaz.site.paymentService.model.web.request.ContractInfo
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueMainContractSubOrderRequestValidator::class, UniqueMainContractContractInfoValidator::class])
annotation class UniqueMainContract(
    val message: String = "Not unique main contract",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class UniqueMainContractSubOrderRequestValidator : ConstraintValidator<UniqueMainContract, List<SubOrderRequest>> {
    override fun isValid(
        orders: List<SubOrderRequest>,
        context: ConstraintValidatorContext?,
    ): Boolean = orders.count { it.mainContractCheck } <= 1
}

class UniqueMainContractContractInfoValidator : ConstraintValidator<UniqueMainContract, List<ContractInfo>> {
    override fun isValid(
        orders: List<ContractInfo>,
        context: ConstraintValidatorContext?,
    ): Boolean = orders.count { it.mainContractCheck } <= 1
}
