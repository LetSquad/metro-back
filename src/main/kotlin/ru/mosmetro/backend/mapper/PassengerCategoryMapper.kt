package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.PassengerCategory
import ru.mosmetro.backend.model.dto.passenger.PassengerCategoryDTO
import ru.mosmetro.backend.model.entity.PassengerCategoryEntity
import ru.mosmetro.backend.model.enums.PassengerCategoryType

@Component
class PassengerCategoryMapper {
    fun entityToDomain(mapper: PassengerCategoryEntity) = PassengerCategory(
        code = PassengerCategoryType.valueOf(mapper.code),
            name = mapper.name,
    )

    fun domainToDto(mapper: PassengerCategory) = PassengerCategoryDTO(
        code = mapper.code.name,
        name = mapper.name,
    )

    fun dtoToDomain(mapper: PassengerCategoryDTO) = PassengerCategory(
        code = PassengerCategoryType.valueOf(mapper.code),
        name = mapper.name,
    )

    fun domainToEntity(mapper: PassengerCategory) = PassengerCategoryEntity(
        code = mapper.code.name,
        name = mapper.name,
    )
}