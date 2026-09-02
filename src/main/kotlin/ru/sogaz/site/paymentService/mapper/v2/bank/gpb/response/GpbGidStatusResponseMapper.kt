package ru.sogaz.site.paymentService.mapper.v2.bank.gpb.response

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import ru.sogaz.site.paymentService.model.v2.bank.enums.GpbExtResultCode
import ru.sogaz.site.paymentService.model.v2.bank.response.BankOperationDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.ClientCardDetails
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.gid.GpbGidStatusCard
import ru.sogaz.site.paymentService.model.v2.bank.response.gpb.gid.GpbGidStatusResponse
import ru.sogaz.site.paymentService.model.v2.enums.OperationState
import java.time.Instant

@Mapper
abstract class GpbGidStatusResponseMapper {
    fun toBankOperationDetails(
        response: GpbGidStatusResponse,
        expectedPaymentBankId: String,
    ): BankOperationDetails =
        if (response.isPendingOrInvalid(expectedPaymentBankId)) {
            BankOperationDetails(
                bankId = expectedPaymentBankId,
                state = OperationState.WAIT,
            )
        } else {
            mapResult(response, expectedPaymentBankId)
        }

    @Mapping(target = "bankId", source = "expectedPaymentBankId")
    @Mapping(target = "state", source = "response.result.status", qualifiedByName = ["toOperationState"])
    @Mapping(target = "operationFinished", source = "response.actualTimestamp", qualifiedByName = ["toInstant"])
    @Mapping(
        target = "extendedCode",
        expression = "java(mapExtendedCode(response.getResult().getStatus(), response.getResult().getExtendedCode()))",
    )
    @Mapping(
        target = "cardDetails",
        expression = "java(mapCardDetails(response.getCard(), response.getResult().getSrcType()))",
    )
    @Mapping(
        target = "errorText",
        expression = "java(mapErrorText(response.getResult().getStatus(), response.getResult().getExtendedCode()))",
    )
    protected abstract fun mapResult(
        response: GpbGidStatusResponse,
        expectedPaymentBankId: String,
    ): BankOperationDetails

    protected fun mapCardDetails(
        card: GpbGidStatusCard?,
        srcType: String?,
    ): ClientCardDetails? = card?.let { mapCard(it, srcType) }

    @Mapping(target = "paymentType", source = "srcType")
    @Mapping(target = "cardId", ignore = true)
    @Mapping(target = "title", ignore = true)
    protected abstract fun mapCard(
        card: GpbGidStatusCard,
        srcType: String?,
    ): ClientCardDetails

    @Named("toOperationState")
    protected fun toOperationState(status: String?): OperationState =
        when (status?.trim()?.uppercase()) {
            "SUCCESS" -> OperationState.SUCCESS
            "FAILED", "REFUND", "DECLINED" -> OperationState.FAIL
            else -> OperationState.WAIT
        }

    @Named("toInstant")
    protected fun toInstant(actualTimestamp: Long): Instant = Instant.ofEpochMilli(actualTimestamp)

    protected fun mapExtendedCode(
        status: String?,
        extendedCode: String?,
    ): String? = extendedCode.takeIf { toOperationState(status) == OperationState.FAIL }

    protected fun mapErrorText(
        status: String?,
        extendedCode: String?,
    ): String? =
        mapExtendedCode(status, extendedCode)
            ?.let { GpbExtResultCode.from(it)?.message ?: it }

    private fun GpbGidStatusResponse.isPendingOrInvalid(expectedPaymentBankId: String): Boolean =
        pgaTrxId != expectedPaymentBankId ||
            !state.equals(RESULT_STATE, ignoreCase = true) ||
            result == null ||
            toOperationState(result.status) == OperationState.WAIT

    companion object {
        private const val RESULT_STATE = "RESULT"
    }
}
