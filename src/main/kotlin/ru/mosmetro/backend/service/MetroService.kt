package ru.mosmetro.backend.service

import kotlinx.coroutines.coroutineScope
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
     * @return список сущностей MetroStationDTO в которых предоставлена информация о станциях матро
     *
     * */
    suspend fun getAllMetroStations(): ListWithTotal<MetroStationDTO> = coroutineScope {
        val employeeDTOList = jpaContext { metroStationEntityRepository.findAll() }
            .map { metroStationMapper.entityToDomain(it) }
            .map { metroStationMapper.domainToDto(it) }
        return@coroutineScope ListWithTotal(employeeDTOList.size, employeeDTOList)
    }
}
