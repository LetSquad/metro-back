package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.MetroStationTransfer
import ru.mosmetro.backend.model.dto.metro.MetroStationTransferDTO
import ru.mosmetro.backend.model.entity.MetroStationEntity
import ru.mosmetro.backend.model.entity.MetroStationTransferEntity

@Component
class MetroStationTransferMapper(
    private val metroStationMapper: MetroStationMapper
) {
    fun entityToDomain(
        mapper: MetroStationTransferEntity,
        startMetroStation: MetroStationEntity,
        finishMetroStation: MetroStationEntity
    ) = MetroStationTransfer(
        startStation = metroStationMapper.entityToDomain(startMetroStation),
        finishStation = metroStationMapper.entityToDomain(finishMetroStation),
        duration = mapper.duration,
        isCrosswalking = mapper.isCrosswalking
    )

    fun domainToDto(mapper: MetroStationTransfer) = MetroStationTransferDTO(
        startStation = metroStationMapper.domainToDto(mapper.startStation),
        finishStation = metroStationMapper.domainToDto(mapper.finishStation),
        duration = mapper.duration,
        isCrosswalking = mapper.isCrosswalking
    )

    fun dtoToDomain(mapper: MetroStationTransferDTO) = MetroStationTransfer(
        startStation = metroStationMapper.dtoToDomain(mapper.startStation),
        finishStation = metroStationMapper.dtoToDomain(mapper.finishStation),
        duration = mapper.duration,
        isCrosswalking = mapper.isCrosswalking
    )
}