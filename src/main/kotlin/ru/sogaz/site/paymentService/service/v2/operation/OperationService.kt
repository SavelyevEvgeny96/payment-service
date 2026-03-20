package ru.sogaz.site.paymentService.service.v2.operation

import ru.sogaz.site.paymentService.model.v2.web.request.OperationRequest
import ru.sogaz.site.paymentService.service.v2.operation.model.OperationCommand

interface OperationService {
    fun <REQUEST : OperationRequest, RESULT> runOperation(operationCommand: OperationCommand<REQUEST, RESULT>): Result<RESULT>
}
