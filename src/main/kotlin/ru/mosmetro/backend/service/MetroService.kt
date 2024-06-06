package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.mapper.MetroStationMapper
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.metro.MetroStationDTO
import ru.mosmetro.backend.repository.MetroStationEntityRepository

@Service
class MetroService(
        private val metroStationEntityRepository: MetroStationEntityRepository,
        private val metroStationMapper: MetroStationMapper,
) {
    /**
     *
     * Метод получает всех рабочих
     *
     * @return список сущностей EmployeeDTO в которых предоставлена информация о рабочих
     *
     * */
    fun getAllMetroStations(): ListWithTotal<MetroStationDTO> {
        val employeeDTOList = metroStationEntityRepository.findAll()
            .map { metroStationMapper.entityToDomain(it) }
            .map { metroStationMapper.domainToDto(it) }
        return ListWithTotal(employeeDTOList.size, employeeDTOList)
    }
}