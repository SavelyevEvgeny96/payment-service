package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.Mapper
import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto

@Mapper(
    componentModel = "spring",
)
interface RegisterCardMapper {
    fun mapErrorBody(src: RegisterCardResponseDto): RegisterCardResponseDto
}
