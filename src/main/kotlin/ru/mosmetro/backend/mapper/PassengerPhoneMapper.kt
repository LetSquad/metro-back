package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.PassengerPhone
import ru.mosmetro.backend.model.dto.passenger.PassengerPhoneDTO
import ru.mosmetro.backend.model.entity.PassengerEntity
import ru.mosmetro.backend.model.entity.PassengerPhoneEntity

@Component
class PassengerPhoneMapper {

    fun entityToDomain(mapper: PassengerPhoneEntity) = PassengerPhone(
        phone = mapper.phoneNumber,
        description = mapper.description
    )

    fun domainToDto(mapper: PassengerPhone) = PassengerPhoneDTO(
        phone = mapper.phone,
        description = mapper.description
    )

    fun dtoToDomain(mapper: PassengerPhoneDTO) = PassengerPhone(
        phone = mapper.phone,
        description = mapper.description
    )

    fun domainToNewEntity(mapper: PassengerPhone, passenger: PassengerEntity) = PassengerPhoneEntity(
        id = null,
        phoneNumber = mapper.phone,
        description = mapper.description,
        passenger = passenger
    )
}
