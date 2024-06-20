package ru.mosmetro.backend.service

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
import ru.mosmetro.backend.model.entity.PassengerPhoneEntity
import ru.mosmetro.backend.repository.PassengerCategoryEntityRepository
import ru.mosmetro.backend.repository.PassengerEntityRepository
import ru.mosmetro.backend.repository.PassengerPhoneEntityRepository
import ru.mosmetro.backend.util.jpaContext

@Service
class PassengerService(
    private val passengerMapper: PassengerMapper,
    private val passengerCategoryMapper: PassengerCategoryMapper,
    private val lockService: EntityLockService,
    private val subscriptionService: EntitySubscriptionService,
    private val passengerEntityRepository: PassengerEntityRepository,
    private val passengerPhoneEntityRepository: PassengerPhoneEntityRepository,
    private val passengerCategoryEntityRepository: PassengerCategoryEntityRepository
) {

    val passengerPhoneCache: Map<Long?, List<PassengerPhoneEntity>> = passengerPhoneEntityRepository.findAll()
        .groupBy({ it.passengerId!! }, { it }) //TODO: обновлять кэш при обновлении телефонов

    /**
     *
     * Метод возвращает список всех пассажиров в системе
     *
     * */
    suspend fun getPassengers(): ListWithTotal<PassengerDTO> {
        val passengerDTOList = jpaContext { passengerEntityRepository.findAll() }
            .map {
                val passengerPhones = passengerPhoneCache.getOrElse(it.id) { emptyList() }
                    .toSet()
                passengerMapper.entityToDomain(it, passengerPhones)
            }
            .map { passengerMapper.domainToDto(it) }
        return ListWithTotal(passengerDTOList.size, passengerDTOList)
    }

    /**
     *
     * Метод возвращает список всех категорий пассажиров в системе
     *
     * */
    suspend fun getPassengerCategories(): ListWithTotal<PassengerCategoryDTO> {
        val passengerCategoryDTOList = jpaContext { passengerCategoryEntityRepository.findAll() }
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
    suspend fun getPassengerById(id: Long): EntityForEdit<PassengerDTO> {
        val passenger: PassengerDTO = jpaContext { passengerEntityRepository.findById(id) }
            .orElseThrow {
                NoSuchPassengerException(id)
            }
            .let {
                val passengerPhone = passengerPhoneEntityRepository.findByPassengerId(it.id!!)
                    .toSet()
                passengerMapper.entityToDomain(it, passengerPhone)
            }
            .let { passengerMapper.domainToDto(it) }

        return EntityForEdit(
            isLockedForEdit = lockService.checkPassengerLock(id),
            data = passenger
        )
    }

    suspend fun getPassengerPhones(id: Long): List<String> {
        return jpaContext { passengerPhoneEntityRepository.findByPassengerId(id) }
            .map { it.phoneNumber }
    }

    /**
     *
     * Метод создает нового пассажира в системе
     *
     * @param newPassengerDTO - модель нового пользователя
     * @return сущность PassengerDTO в которой предоставлена информация о пассажире
     *
     * */
    suspend fun createPassenger(newPassengerDTO: NewPassengerDTO): PassengerDTO {
        return newPassengerDTO
            .let { passengerMapper.dtoToDomain(it) }
            .let { passengerMapper.domainToEntity(it) }
            .let { jpaContext { passengerEntityRepository.save(it) } }
            .also { subscriptionService.notifyPassengerUpdate() }
            .let { passengerMapper.entityToDomain(it, emptySet()) }
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
    suspend fun updatePassenger(id: Long, updatePassengerDTO: UpdatePassengerDTO): PassengerDTO {
        val passengerEntity = jpaContext { passengerEntityRepository.findById(id) }
            .orElseThrow { NoSuchPassengerException(id) }

        return updatePassengerDTO
            .let { passengerMapper.dtoToDomain(it, id, passengerEntity.createdAt) }
            .let { passengerMapper.domainToEntity(it) }
            .let { passengerEntityRepository.save(it) }
            .also { subscriptionService.notifyPassengerUpdate() }
            .let { passengerMapper.entityToDomain(it, emptySet()) }
            .let { passengerMapper.domainToDto(it) }
    }

    /**
     *
     * Метод удаляет существующего пассажира в системе
     *
     * @param id - идентификатор пользователя
     *
     * */
    suspend fun deletePassenger(id: Long) {
        jpaContext { passengerEntityRepository.deleteById(id) }
            .also { subscriptionService.notifyPassengerUpdate() }
    }
}
