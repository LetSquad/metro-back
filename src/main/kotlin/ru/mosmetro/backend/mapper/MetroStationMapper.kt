package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.MetroStation
import ru.mosmetro.backend.model.dto.metro.MetroStationDTO
import ru.mosmetro.backend.model.entity.MetroStationEntity

@Component
class MetroStationMapper(
        private val metroLineMapper: MetroLineMapper,
) {
    fun entityToDomain(mapper: MetroStationEntity) = MetroStation(
            id = mapper.id,
            name = mapper.name,
            line = mapper.line?.let { metroLineMapper.entityToDomain(it) },
    )

    fun domainToDto(mapper: MetroStation) = MetroStationDTO(
            id = mapper.id,
            name = mapper.name,
            line = mapper.line?.let { metroLineMapper.domainToDto(it) },
    )
}