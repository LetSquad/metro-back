package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.Passenger
import ru.mosmetro.backend.model.domain.PassengerCategory
import ru.mosmetro.backend.model.dto.passenger.NewPassengerDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerDTO
import ru.mosmetro.backend.model.dto.passenger.UpdatePassengerDTO
import ru.mosmetro.backend.model.entity.PassengerEntity
import ru.mosmetro.backend.model.entity.PassengerPhoneEntity
import ru.mosmetro.backend.model.enums.SexType
import java.time.Instant

@Component
class PassengerMapper(
    private val passengerCategoryMapper: PassengerCategoryMapper,
    private val passengerPhoneMapper: PassengerPhoneMapper
) {

    fun entityToDomain(mapper: PassengerEntity, passengerPhoneEntity: Set<PassengerPhoneEntity>) = Passenger(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = SexType.valueOf(mapper.sex),
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = mapper.createdAt,
        deletedAt = mapper.deletedAt,
        category = passengerCategoryMapper.entityToDomain(mapper.category),
        phones = passengerPhoneEntity.map { passengerPhoneMapper.entityToDomain(it) }.toSet()
    )

    fun dtoToDomain(mapper: NewPassengerDTO, category: PassengerCategory) = Passenger(
        id = null,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = Instant.now(),
        deletedAt = null,
        category = category,
        phones = emptySet()
    )

    fun dtoToDomain(mapper: PassengerDTO) = Passenger(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = Instant.now(),
        deletedAt = null,
        category = passengerCategoryMapper.dtoToDomain(mapper.category),
        phones = mapper.phones.map { passengerPhoneMapper.dtoToDomain(it) }.toSet()
    )

    fun dtoToDomain(mapper: UpdatePassengerDTO, id: Long, createdAt: Instant, category: PassengerCategory) = Passenger(
        id = id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = createdAt,
        deletedAt = null,
        category = category,
        phones = emptySet()
    )

    fun domainToEntity(mapper: Passenger) = PassengerEntity(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex.name,
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = mapper.createdAt,
        deletedAt = mapper.deletedAt,
        category = passengerCategoryMapper.domainToEntity(mapper.category)
    )

    fun domainToDto(mapper: Passenger) = PassengerDTO(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        category = passengerCategoryMapper.domainToDto(mapper.category),
        phones = mapper.phones.map { passengerPhoneMapper.domainToDto(it) }.toSet()
    )
}