package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.MetroLine
import ru.mosmetro.backend.model.dto.metro.MetroLineDTO
import ru.mosmetro.backend.model.entity.MetroLineEntity

@Component
class MetroLineMapper {
    fun entityToDomain(mapper: MetroLineEntity) = MetroLine(
            id = mapper.id,
            name = mapper.name,
    )

    fun domainToDto(mapper: MetroLine) = MetroLineDTO(
            id = mapper.id,
            name = mapper.name,
    )
}