package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.Passenger
import ru.mosmetro.backend.model.dto.passenger.NewPassengerDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerPhoneDTO
import ru.mosmetro.backend.model.dto.passenger.UpdatePassengerDTO
import ru.mosmetro.backend.model.entity.PassengerEntity
import ru.mosmetro.backend.model.enums.SexType
import java.time.Instant

@Component
class PassengerMapper(private val passengerCategoryMapper: PassengerCategoryMapper) {

    fun entityToDomain(mapper: PassengerEntity) = Passenger(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = SexType.valueOf(mapper.sex),
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = mapper.createdAt,
        deletedAt = mapper.deletedAt,
        category = passengerCategoryMapper.entityToDomain(mapper.category)
    )

    fun dtoToDomain(mapper: NewPassengerDTO) = Passenger(
        id = null,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = Instant.now(),
        deletedAt = null,
        category = passengerCategoryMapper.dtoToDomain(mapper.category)
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
        category = passengerCategoryMapper.dtoToDomain(mapper.category)
    )

    fun dtoToDomain(mapper: UpdatePassengerDTO, id: Long, createdAt: Instant) = Passenger(
        id = id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = createdAt,
        deletedAt = mapper.deletedAt,
        category = passengerCategoryMapper.dtoToDomain(mapper.category)
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
        // TODO dirty hack
        phones = setOf(
            PassengerPhoneDTO("79099321234", "Телефон личный"),
            PassengerPhoneDTO("79099321234", "Телефон рабочий"),
            PassengerPhoneDTO("79099321234", "Телефон сопровождающего")
        )
    )
}