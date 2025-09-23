package ru.sogaz.site.paymentService.dto.exceptions

import ru.sogaz.site.paymentService.enums.ActionType

class BankIntegrationException(
    val actionType: ActionType,
) : RuntimeException(actionType.value)
