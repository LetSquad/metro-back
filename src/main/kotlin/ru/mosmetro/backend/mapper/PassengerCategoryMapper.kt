package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.PassengerCategory
import ru.mosmetro.backend.model.dto.passenger.PassengerCategoryDTO
import ru.mosmetro.backend.model.entity.PassengerCategoryEntity

@Component
class PassengerCategoryMapper {
    fun entityToDomain(mapper: PassengerCategoryEntity) = PassengerCategory(
            code = mapper.code,
            name = mapper.name,
    )

    fun domainToDto(mapper: PassengerCategory) = PassengerCategoryDTO(
            code = mapper.code,
            name = mapper.name,
    )
}