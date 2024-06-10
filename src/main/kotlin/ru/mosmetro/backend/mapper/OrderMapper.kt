package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.OrderApplication
import ru.mosmetro.backend.model.domain.OrderBaggage
import ru.mosmetro.backend.model.domain.OrderStatus
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.dto.order.NewPassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.OrderApplicationDTO
import ru.mosmetro.backend.model.dto.order.OrderBaggageDTO
import ru.mosmetro.backend.model.dto.order.OrderStatusDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.model.entity.OrderStatusEntity
import ru.mosmetro.backend.model.entity.PassengerOrderEntity
import ru.mosmetro.backend.model.enums.OrderStatusType
import java.time.Duration
import java.time.Instant

@Component
class OrderMapper(
    private val metroStationMapper: MetroStationMapper,
    private val metroStationTransferMapper: MetroStationTransferMapper,
    private val passengerMapper: PassengerMapper,
    private val passengerCategoryMapper: PassengerCategoryMapper
) {
    fun entityToDomain(mapper: PassengerOrderEntity) = PassengerOrder(
        id = mapper.id,
        startDescription = mapper.startDescription,
        finishDescription = mapper.finishDescription,
        orderApplication = OrderApplication(
            mapper.orderStatusCode.code,
            mapper.orderStatusCode.name
        ),
        passengerCount = mapper.passengerCount,
        maleEmployeeCount = mapper.maleEmployeeCount,
        femaleEmployeeCount = mapper.femaleEmployeeCount,
        additionalInfo = mapper.additionalInfo,
        orderTime = mapper.orderTime,
        startTime = mapper.startTime,
        finishTime = mapper.finishTime,
        absenceTime = mapper.absenceTime,
        cancelTime = mapper.cancelTime,
        baggage = mapper.baggage,
        startMetroStation = metroStationMapper.entityToDomain(mapper.startStation),
        finishMetroStation = metroStationMapper.entityToDomain(mapper.finishStation),
        orderStatus = OrderStatus(OrderStatusType.valueOf(mapper.orderStatusCode.code), mapper.orderStatusCode.name),
        passenger = passengerMapper.entityToDomain(mapper.passenger),
        passengerCategory = passengerCategoryMapper.entityToDomain(mapper.passengerCategory),
        transfers = mapper.transfers,
        createdAt = mapper.createdAt,
        updatedAt = mapper.updatedAt,
        deletedAt = mapper.deletedAt,
        duration = mapper.duration
    )

    fun domainToDto(mapper: PassengerOrder) = PassengerOrderDTO(
        id = mapper.id,
        startDescription = mapper.startDescription,
        finishDescription = mapper.finishDescription,
        orderApplication = OrderApplicationDTO(
            mapper.orderApplication.code,
            mapper.orderApplication.name
        ),
        passengerCount = mapper.passengerCount,
        maleEmployeeCount = mapper.maleEmployeeCount,
        femaleEmployeeCount = mapper.femaleEmployeeCount,
        additionalInfo = mapper.additionalInfo,
        orderTime = mapper.orderTime,
        startTime = mapper.startTime,
        finishTime = mapper.finishTime,
        absenceTime = mapper.absenceTime,
        cancelTime = mapper.cancelTime,
        baggage = if (mapper.baggage != null) OrderBaggageDTO(
            mapper.baggage.type,
            mapper.baggage.weight, mapper.baggage.isHelpNeeded
        ) else null,
        duration = mapper.duration.toSeconds(),
        startStation = metroStationMapper.domainToDto(mapper.startMetroStation),
        finishStation = metroStationMapper.domainToDto(mapper.finishMetroStation),
        orderStatus = OrderStatusDTO(mapper.orderStatus.code, mapper.orderStatus.name),
        passenger = passengerMapper.domainToDto(mapper.passenger),
        passengerCategory = passengerCategoryMapper.domainToDto(mapper.passengerCategory),
        transfers = mapper.transfers.map { metroStationTransferMapper.domainToDto(it) }
    )

    fun dtoToDomain(mapper: NewPassengerOrderDTO) = PassengerOrder(
        id = null,
        startDescription = mapper.startDescription,
        finishDescription = mapper.finishDescription,
        orderApplication = OrderApplication(mapper.orderApplication.code, mapper.orderApplication.name),
        passengerCount = mapper.passengerCount,
        maleEmployeeCount = mapper.maleEmployeeCount,
        femaleEmployeeCount = mapper.femaleEmployeeCount,
        additionalInfo = mapper.additionalInfo,
        orderTime = mapper.orderTime,
        startTime = mapper.startTime,
        finishTime = mapper.finishTime,
        absenceTime = null,
        cancelTime = null,
        createdAt = Instant.now(),
        updatedAt = null,
        deletedAt = null,
        baggage = if (mapper.baggage != null) OrderBaggage(
            mapper.baggage.type,
            mapper.baggage.weight,
            mapper.baggage.isHelpNeeded
        ) else null,
        startMetroStation = metroStationMapper.dtoToDomain(mapper.startMetroStation),
        finishMetroStation = metroStationMapper.dtoToDomain(mapper.finishMetroStation),
        orderStatus = OrderStatus(mapper.orderStatus.code, mapper.orderStatus.name),
        passenger = passengerMapper.dtoToDomain(mapper.passenger),
        passengerCategory = passengerCategoryMapper.dtoToDomain(mapper.passengerCategory),
        transfers = mapper.transfers.map { metroStationTransferMapper.dtoToDomain(it) },
        duration = Duration.ofSeconds(mapper.duration)
    )

    fun dtoToDomain(mapper: UpdatedPassengerOrderDTO, createdAt: Instant) = PassengerOrder(
        id = null,
        startDescription = mapper.startDescription,
        finishDescription = mapper.finishDescription,
        orderApplication = OrderApplication(mapper.orderApplication.code, mapper.orderApplication.name),
        passengerCount = mapper.passengerCount,
        maleEmployeeCount = mapper.maleEmployeeCount,
        femaleEmployeeCount = mapper.femaleEmployeeCount,
        additionalInfo = mapper.additionalInfo,
        orderTime = mapper.orderTime,
        startTime = mapper.startTime,
        finishTime = mapper.finishTime,
        absenceTime = null,
        cancelTime = null,
        createdAt = createdAt,
        updatedAt = Instant.now(),
        deletedAt = null,
        baggage = if (mapper.baggage != null) OrderBaggage(
            mapper.baggage.type,
            mapper.baggage.weight,
            mapper.baggage.isHelpNeeded
        ) else null,
        startMetroStation = metroStationMapper.dtoToDomain(mapper.startMetroStation),
        finishMetroStation = metroStationMapper.dtoToDomain(mapper.finishMetroStation),
        orderStatus = OrderStatus(mapper.orderStatus.code, mapper.orderStatus.name),
        passenger = passengerMapper.dtoToDomain(mapper.passenger),
        passengerCategory = passengerCategoryMapper.dtoToDomain(mapper.passengerCategory),
        transfers = mapper.transfers.map { metroStationTransferMapper.dtoToDomain(it) },
        duration = Duration.ofSeconds(mapper.duration)
    )

    fun domainToEntity(mapper: PassengerOrder) = PassengerOrderEntity(
        id = mapper.id,
        startDescription = mapper.startDescription,
        finishDescription = mapper.finishDescription,
        passengerCount = mapper.passengerCount,
        maleEmployeeCount = mapper.maleEmployeeCount,
        femaleEmployeeCount = mapper.femaleEmployeeCount,
        additionalInfo = mapper.additionalInfo,
        orderTime = mapper.orderTime,
        startTime = mapper.startTime,
        finishTime = mapper.finishTime,
        absenceTime = mapper.absenceTime,
        cancelTime = mapper.cancelTime,
        createdAt = mapper.createdAt,
        updatedAt = mapper.updatedAt,
        deletedAt = mapper.deletedAt,
        startStation = metroStationMapper.domainToEntity(mapper.startMetroStation),
        finishStation = metroStationMapper.domainToEntity(mapper.finishMetroStation),
        baggage = mapper.baggage,
        orderStatusCode = OrderStatusEntity(mapper.orderApplication.code, mapper.orderStatus.name),
        passenger = passengerMapper.domainToEntity(mapper.passenger),
        passengerCategory = passengerCategoryMapper.domainToEntity(mapper.passengerCategory),
        transfers = mapper.transfers,
        duration = mapper.duration
    )
}