package ru.mosmetro.backend.mapper

import java.time.Instant
import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.dto.order.NewPassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.model.entity.PassengerOrderEntity

@Component
class OrderMapper {
    fun entityToDomain(passengerOrderEntity: PassengerOrderEntity) = PassengerOrder(
        id = passengerOrderEntity.id,
        startDescription = passengerOrderEntity.startDescription,
        finishDescription = passengerOrderEntity.finishDescription,
        orderApplication = passengerOrderEntity.orderApplication,
        passengerCount = passengerOrderEntity.passengerCount,
        maleEmployeeCount = passengerOrderEntity.maleEmployeeCount,
        femaleEmployeeCount = passengerOrderEntity.femaleEmployeeCount,
        additionalInfo = passengerOrderEntity.additionalInfo,
        orderTime = passengerOrderEntity.orderTime,
        startTime = passengerOrderEntity.startTime,
        finishTime = passengerOrderEntity.finishTime,
        absenceTime = passengerOrderEntity.absenceTime,
        cancelTime = passengerOrderEntity.cancelTime,
        createdAt = passengerOrderEntity.createdAt,
        updatedAt = passengerOrderEntity.updatedAt,
        deletedAt = passengerOrderEntity.deletedAt
    )

    fun domainToDto(passengerOrder: PassengerOrder) = PassengerOrderDTO(
        id = passengerOrder.id,
        startDescription = passengerOrder.startDescription,
        finishDescription = passengerOrder.finishDescription,
        orderApplication = passengerOrder.orderApplication,
        passengerCount = passengerOrder.passengerCount,
        maleEmployeeCount = passengerOrder.maleEmployeeCount,
        femaleEmployeeCount = passengerOrder.femaleEmployeeCount,
        additionalInfo = passengerOrder.additionalInfo,
        orderTime = passengerOrder.orderTime,
        startTime = passengerOrder.startTime,
        finishTime = passengerOrder.finishTime,
        absenceTime = passengerOrder.absenceTime,
        cancelTime = passengerOrder.cancelTime,
        createdAt = passengerOrder.createdAt,
        updatedAt = passengerOrder.updatedAt,
        deletedAt = passengerOrder.deletedAt
    )

    fun dtoToDomain(newPassengerOrderDTO: NewPassengerOrderDTO) = PassengerOrder(
        id = null,
        startDescription = newPassengerOrderDTO.startDescription,
        finishDescription = newPassengerOrderDTO.finishDescription,
        orderApplication = newPassengerOrderDTO.orderApplication,
        passengerCount = newPassengerOrderDTO.passengerCount,
        maleEmployeeCount = newPassengerOrderDTO.maleEmployeeCount,
        femaleEmployeeCount = newPassengerOrderDTO.femaleEmployeeCount,
        additionalInfo = newPassengerOrderDTO.additionalInfo,
        orderTime = newPassengerOrderDTO.orderTime,
        startTime = newPassengerOrderDTO.startTime,
        finishTime = newPassengerOrderDTO.finishTime,
        absenceTime = null,
        cancelTime = null,
        createdAt = null,
        updatedAt = null,
        deletedAt = null
    )

    fun dtoToDomain(updatedPassengerOrderDTO: UpdatedPassengerOrderDTO) = PassengerOrder(
        id = null,
        startDescription = updatedPassengerOrderDTO.startDescription,
        finishDescription = updatedPassengerOrderDTO.finishDescription,
        orderApplication = updatedPassengerOrderDTO.orderApplication,
        passengerCount = updatedPassengerOrderDTO.passengerCount,
        maleEmployeeCount = updatedPassengerOrderDTO.maleEmployeeCount,
        femaleEmployeeCount = updatedPassengerOrderDTO.femaleEmployeeCount,
        additionalInfo = updatedPassengerOrderDTO.additionalInfo,
        orderTime = updatedPassengerOrderDTO.orderTime,
        startTime = updatedPassengerOrderDTO.startTime,
        finishTime = updatedPassengerOrderDTO.finishTime,
        absenceTime = null,
        cancelTime = null,
        createdAt = null,
        updatedAt = Instant.now(),
        deletedAt = null
    )

    fun domainToEntity(passengerOrder: PassengerOrder) = PassengerOrderEntity(
        id = null,
        startDescription = passengerOrder.startDescription,
        finishDescription = passengerOrder.finishDescription,
        orderApplication = passengerOrder.orderApplication,
        passengerCount = passengerOrder.passengerCount,
        maleEmployeeCount = passengerOrder.maleEmployeeCount,
        femaleEmployeeCount = passengerOrder.femaleEmployeeCount,
        additionalInfo = passengerOrder.additionalInfo,
        orderTime = passengerOrder.orderTime,
        startTime = passengerOrder.startTime,
        finishTime = passengerOrder.finishTime,
        absenceTime = passengerOrder.absenceTime,
        cancelTime = passengerOrder.cancelTime,
        createdAt = passengerOrder.createdAt,
        updatedAt = passengerOrder.updatedAt,
        deletedAt = passengerOrder.deletedAt,
        startStation = null,
        finishStation = null,
        baggage = null,
        orderStatusCode = null,
        passenger = null,
        passengerCategory = null,
        transfers = null
    )
}