package ru.mosmetro.backend.service

import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import ru.mosmetro.backend.exception.NoSuchPassengerException
import ru.mosmetro.backend.mapper.PassengerCategoryMapper
import ru.mosmetro.backend.mapper.PassengerMapper
import ru.mosmetro.backend.model.dto.EntityForEdit
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.passenger.NewPassengerDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerCategoryDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerDTO
import ru.mosmetro.backend.model.dto.passenger.UpdatePassengerDTO
import ru.mosmetro.backend.repository.PassengerCategoryEntityRepository
import ru.mosmetro.backend.repository.PassengerEntityRepository
import ru.mosmetro.backend.util.jpaContext

@Service
class PassengerService(
    private val passengerMapper: PassengerMapper,
    private val passengerCategoryMapper: PassengerCategoryMapper,
    private val lockService: EntityLockService,
    private val subscriptionService: EntitySubscriptionService,
    private val passengerEntityRepository: PassengerEntityRepository,
    private val passengerCategoryEntityRepository: PassengerCategoryEntityRepository
) {

    /**
     *
     * Метод возвращает список всех пассажиров в системе
     *
     * */
    suspend fun getPassengers(): ListWithTotal<PassengerDTO> = coroutineScope {
        val passengerDTOList = jpaContext { passengerEntityRepository.findAll() }
            .map { passengerMapper.entityToDomain(it) }
            .map { passengerMapper.domainToDto(it) }
        return@coroutineScope ListWithTotal(passengerDTOList.size, passengerDTOList)
    }

    /**
     *
     * Метод возвращает список всех категорий пассажиров в системе
     *
     * */
    suspend fun getPassengerCategories(): ListWithTotal<PassengerCategoryDTO> = coroutineScope {
        val passengerCategoryDTOList = jpaContext { passengerCategoryEntityRepository.findAll() }
            .map { passengerCategoryMapper.entityToDomain(it) }
            .map { passengerCategoryMapper.domainToDto(it) }
        return@coroutineScope ListWithTotal(passengerCategoryDTOList.size, passengerCategoryDTOList)
    }

    /**
     *
     * Метод возвращает пассажира по его идентификатору
     *
     * @param id - идентификатор пользователя
     * @return сущность PassengerDTO в которой предоставлена информация о пассажире
     *
     * */
    suspend fun getPassengerById(id: Long): EntityForEdit<PassengerDTO> = coroutineScope {
        val passenger: PassengerDTO = jpaContext { passengerEntityRepository.findById(id) }
            .orElseThrow {
                NoSuchPassengerException(id)
            }
            .let { passengerMapper.entityToDomain(it) }
            .let { passengerMapper.domainToDto(it) }

        return@coroutineScope EntityForEdit(
            isLockedForEdit = lockService.checkPassengerLock(id),
            data = passenger
        )
    }

    /**
     *
     * Метод создает нового пассажира в системе
     *
     * @param newPassengerDTO - модель нового пользователя
     * @return сущность PassengerDTO в которой предоставлена информация о пассажире
     *
     * */
    suspend fun createPassenger(newPassengerDTO: NewPassengerDTO): PassengerDTO = coroutineScope {
        return@coroutineScope newPassengerDTO
            .let { passengerMapper.dtoToDomain(it) }
            .let { passengerMapper.domainToEntity(it) }
            .let { jpaContext { passengerEntityRepository.save(it) } }
            .also { subscriptionService.notifyPassengerUpdate() }
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
    suspend fun updatePassenger(id: Long, updatePassengerDTO: UpdatePassengerDTO): PassengerDTO = coroutineScope {
        val passengerEntity = jpaContext { passengerEntityRepository.findById(id) }
            .orElseThrow { NoSuchPassengerException(id) }

        return@coroutineScope updatePassengerDTO
            .let { passengerMapper.dtoToDomain(it, id, passengerEntity.createdAt) }
            .let { passengerMapper.domainToEntity(it) }
            .let { passengerEntityRepository.save(it) }
            .also { subscriptionService.notifyPassengerUpdate() }
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
    suspend fun deletePassenger(id: Long) = coroutineScope {
        jpaContext { passengerEntityRepository.deleteById(id) }
            .also { subscriptionService.notifyPassengerUpdate() }
    }
}
