package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.MetroStationTransfer
import ru.mosmetro.backend.model.dto.metro.MetroStationTransferDTO
import ru.mosmetro.backend.model.entity.MetroStationTransferEntity
import java.time.Duration

@Component
class MetroStationTransferMapper(
    private val metroStationMapper: MetroStationMapper
) {

    fun entityToDomain(mapper: MetroStationTransferEntity) = MetroStationTransfer(
        startStation = metroStationMapper.entityToDomain(mapper.startStation),
        finishStation = metroStationMapper.entityToDomain(mapper.finishStation),
        duration = mapper.duration,
        isCrosswalking = mapper.isCrosswalking
    )

    fun domainToDto(mapper: MetroStationTransfer) = MetroStationTransferDTO(
        startStation = metroStationMapper.domainToDto(mapper.startStation),
        finishStation = metroStationMapper.domainToDto(mapper.finishStation),
        duration = mapper.duration.toSeconds(),
        isCrosswalking = mapper.isCrosswalking
    )

    fun dtoToDomain(mapper: MetroStationTransferDTO) = MetroStationTransfer(
        startStation = metroStationMapper.dtoToDomain(mapper.startStation),
        finishStation = metroStationMapper.dtoToDomain(mapper.finishStation),
        duration = Duration.ofSeconds(mapper.duration),
        isCrosswalking = mapper.isCrosswalking
    )
}
