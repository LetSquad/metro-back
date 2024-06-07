package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.exception.NoSuchPassengerException
import ru.mosmetro.backend.mapper.PassengerCategoryMapper
import ru.mosmetro.backend.mapper.PassengerMapper
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.passenger.NewPassengerDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerCategoryDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerDTO
import ru.mosmetro.backend.model.dto.passenger.UpdatePassengerDTO
import ru.mosmetro.backend.repository.PassengerCategoryEntityRepository
import ru.mosmetro.backend.repository.PassengerEntityRepository

@Service
class PassengerService(
        private val passengerMapper: PassengerMapper,
        private val passengerCategoryMapper: PassengerCategoryMapper,
        private val passengerEntityRepository: PassengerEntityRepository,
        private val passengerCategoryEntityRepository: PassengerCategoryEntityRepository
) {
    /**
     *
     * Метод возвращает список всех пассажиров в системе
     *
     * */
    fun getPassengers(): ListWithTotal<PassengerDTO> {
        val passengerDTOList = passengerEntityRepository.findAll()
            .map { passengerMapper.entityToDomain(it) }
            .map { passengerMapper.domainToDto(it) }
        return ListWithTotal(passengerDTOList.size, passengerDTOList)
    }

    /**
     *
     * Метод возвращает список всех категорий пассажиров в системе
     *
     * */
    fun getPassengerCategories(): ListWithTotal<PassengerCategoryDTO> {
        val passengerCategoryDTOList = passengerCategoryEntityRepository.findAll()
            .map { passengerCategoryMapper.entityToDomain(it) }
            .map { passengerCategoryMapper.domainToDto(it) }
        return ListWithTotal(passengerCategoryDTOList.size, passengerCategoryDTOList)
    }

    /**
     *
     * Метод возвращает пассажира по его идентификатору
     *
     * @param id - идентификатор пользователя
     * @return сущность PassengerDTO в которой предоставлена информация о пассажире
     *
     * */
    fun getPassengerById(id: Long): PassengerDTO {
        return passengerEntityRepository.findById(id)
            .orElseThrow {
                NoSuchPassengerException(id)
            }
            .let { passengerMapper.entityToDomain(it) }
            .let { passengerMapper.domainToDto(it) }
    }

    /**
     *
     * Метод создает нового пассажира в системе
     *
     * @param newPassengerDTO - модель нового пользователя
     * @return сущность PassengerDTO в которой предоставлена информация о пассажире
     *
     * */
    fun createPassenger(newPassengerDTO: NewPassengerDTO): PassengerDTO {
        return newPassengerDTO
            .let { passengerMapper.dtoToDomain(it) }
            .let { passengerMapper.domainToEntity(it, newPassengerDTO.category.name) }
            .let { passengerEntityRepository.save(it) }
            .let { passengerMapper.entityToDomain(it) }
            .let { passengerMapper.domainToDto(it) }
    }

    /**
     *
     * Метод изменяет существующего пассажира в системе
     *
     * @param updatePassengerDTO - модель обновленной информации о пользователе
     * @return сущность PassengerDTO в которой предоставлена информация о пассажире
     *
     * */
    fun updatePassenger(id: Long, updatePassengerDTO: UpdatePassengerDTO): PassengerDTO {
        val passengerEntity = passengerEntityRepository.findById(id)
            .orElseThrow {
                NoSuchPassengerException(id)
            }

        return updatePassengerDTO
            .let { passengerMapper.dtoToDomain(it, id, passengerEntity.createdAt) }
            .let { passengerMapper.domainToEntity(it, passengerEntity.category.name) }
            .let { passengerEntityRepository.save(it) }
            .let { passengerMapper.entityToDomain(it) }
            .let { passengerMapper.domainToDto(it) }
    }

    /**
     *
     * Метод удаляет существующего пассажира в системе
     *
     * @param id - идентификатор пользователя
     *
     * */
    fun deletePassenger(id: Long) {
        passengerEntityRepository.deleteById(id)
    }
}