package ru.mosmetro.backend.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import ru.mosmetro.backend.mapper.MetroStationMapper
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.metro.MetroStationDTO
import ru.mosmetro.backend.repository.MetroStationEntityRepository
import ru.mosmetro.backend.util.jpaContext

@Service
class MetroService(
    private val metroStationEntityRepository: MetroStationEntityRepository,
    private val metroStationMapper: MetroStationMapper,
) {

    /**
     *
     * Метод получает все станции метро
     *
     * @return список сущностей MetroStationDTO в которых предоставлена информация о станциях метро
     *
     * */
    suspend fun getAllMetroStations(): ListWithTotal<MetroStationDTO> {
        val metroStationDTOList = jpaContext { metroStationEntityRepository.findAll() }
            .map { metroStationMapper.entityToDomain(it) }
            .map { metroStationMapper.domainToDto(it) }
        return ListWithTotal(metroStationDTOList.size, metroStationDTOList)
    }

    /**
     *
     * Метод станцию метро по её идентификатору
     *
     * @return MetroStationDTO в которых предоставлена информация о станциях метро
     *
     * */
    suspend fun getMetroStationById(id: Long): MetroStationDTO {
        val metroStation = jpaContext { metroStationEntityRepository.findById(id) }
            .orElseThrow { EntityNotFoundException(id.toString()) }
            .let { metroStationMapper.entityToDomain(it) }
            .let { metroStationMapper.domainToDto(it) }
        return metroStation
    }
}
