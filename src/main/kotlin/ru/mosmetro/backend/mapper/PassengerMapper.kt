package ru.mosmetro.backend.mapper

import java.time.Instant
import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.Passenger
import ru.mosmetro.backend.model.dto.passenger.NewPassengerDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerDTO
import ru.mosmetro.backend.model.dto.passenger.UpdatePassengerDTO
import ru.mosmetro.backend.model.entity.PassengerEntity
import ru.mosmetro.backend.model.enums.SexType

@Component
class PassengerMapper {
    fun entityToDomain(mapper: PassengerEntity) = Passenger(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = SexType.valueOf(mapper.sex),
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = mapper.createdAt,
        deletedAt = mapper.deletedAt
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
        deletedAt = null
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
        deletedAt = mapper.deletedAt
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
        deletedAt = mapper.deletedAt
    )

    fun domainToDto(mapper: Passenger) = PassengerDTO(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        comment = mapper.comment,
        hasPacemaker = mapper.hasPacemaker,
        createdAt = mapper.createdAt,
        deletedAt = mapper.deletedAt
    )
}