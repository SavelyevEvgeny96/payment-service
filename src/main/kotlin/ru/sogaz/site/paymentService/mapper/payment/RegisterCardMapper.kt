package ru.sogaz.site.paymentService.mapper.payment

import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import ru.sogaz.site.paymentService.dto.response.bank.RegisterCardResponseDto

@Mapper(componentModel = "spring")
interface RegisterCardMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mappings(
        Mapping(target = "error", source = "error"),
        Mapping(target = "side", source = "side"),
        Mapping(target = "state", constant = "ERROR"),
    )
    fun mapErrorBody(src: RegisterCardResponseDto): RegisterCardResponseDto
}
