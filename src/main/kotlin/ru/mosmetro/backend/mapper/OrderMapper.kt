package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.MetroStation
import ru.mosmetro.backend.model.domain.OrderBaggage
import ru.mosmetro.backend.model.domain.OrderStatus
import ru.mosmetro.backend.model.domain.Passenger
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.dto.order.NewPassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.OrderBaggageDTO
import ru.mosmetro.backend.model.dto.order.OrderStatusDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.dto.order.UpdatedPassengerOrderDTO
import ru.mosmetro.backend.model.entity.MetroStationEntity
import ru.mosmetro.backend.model.entity.OrderStatusEntity
import ru.mosmetro.backend.model.entity.PassengerEntity
import ru.mosmetro.backend.model.entity.PassengerOrderEntity
import ru.mosmetro.backend.model.enums.OrderApplicationType
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.model.enums.PassengerCategoryType
import ru.mosmetro.backend.service.MetroTransfersService
import java.time.Duration
import java.time.Instant

@Component
class OrderMapper(
    private val metroTransfersService: MetroTransfersService,
    private val metroStationMapper: MetroStationMapper,
    private val metroStationTransferMapper: MetroStationTransferMapper,
    private val passengerMapper: PassengerMapper,
) {
    fun entityToDomain(mapper: PassengerOrderEntity) = PassengerOrder(
        id = mapper.id,
        startDescription = "Встретить в фойе метро центре станции",
        finishDescription = "Отвезти до выхода 3, там встретят",
        orderApplication = if (mapper.orderApplication != null) OrderApplicationType.valueOf(mapper.orderApplication) else null,
        passengerCount = mapper.passengerCount,
        maleEmployeeCount = mapper.maleEmployeeCount,
        femaleEmployeeCount = mapper.femaleEmployeeCount,
        additionalInfo = "Пассажир с одним сопровождающим в черной шляпе и синем пальто",
        orderTime = mapper.orderTime,
        startTime = mapper.startTime,
        finishTime = mapper.finishTime,
        absenceTime = mapper.absenceTime,
        cancelTime = mapper.cancelTime,
        baggage = mapper.baggage,
        startStation = metroStationMapper.entityToDomain(mapper.startStation),
        finishStation = metroStationMapper.entityToDomain(mapper.finishStation),
        orderStatus = OrderStatus(OrderStatusType.valueOf(mapper.orderStatusCode.code), mapper.orderStatusCode.name),
        passenger = passengerMapper.entityToDomain(mapper.passenger),
        passengerCategory = PassengerCategoryType.valueOf(mapper.passengerCategory?.code ?: mapper.passenger.category.code),
        transfers = metroTransfersService.calculateTransfers(mapper.startStation.id!!, mapper.finishStation.id!!).transfers,
        createdAt = mapper.createdAt,
        updatedAt = mapper.updatedAt,
        deletedAt = mapper.deletedAt,
        duration = mapper.duration,
        //TODO откуда брать?
        employees = emptySet()
    )

    fun domainToDto(mapper: PassengerOrder) = PassengerOrderDTO(
        id = mapper.id,
        startDescription = mapper.startDescription,
        finishDescription = mapper.finishDescription,
        orderApplication = mapper.orderApplication,
        passengerCount = mapper.passengerCount,
        maleEmployeeCount = mapper.maleEmployeeCount,
        femaleEmployeeCount = mapper.femaleEmployeeCount,
        additionalInfo = mapper.additionalInfo,
        orderTime = mapper.orderTime,
        startTime = mapper.startTime,
        finishTime = mapper.finishTime,
        absenceTime = mapper.absenceTime,
        cancelTime = mapper.cancelTime,
        baggage = if (mapper.baggage != null) {
            OrderBaggageDTO(
                mapper.baggage.type,
                mapper.baggage.weight, mapper.baggage.isHelpNeeded
            )
        } else {
            null
        },
        duration = mapper.duration.toSeconds(),
        startStation = metroStationMapper.domainToDto(mapper.startStation),
        finishStation = metroStationMapper.domainToDto(mapper.finishStation),
        orderStatus = OrderStatusDTO(mapper.orderStatus.code, mapper.orderStatus.name),
        passenger = passengerMapper.domainToDto(mapper.passenger),
        passengerCategory = mapper.passengerCategory,
        transfers = mapper.transfers.map { metroStationTransferMapper.domainToDto(it) },
        employees = emptySet()
    )

    fun dtoToDomain(mapper: NewPassengerOrderDTO, startStation: MetroStation, finishStation: MetroStation, passenger: Passenger) = PassengerOrder(
        id = null,
        startDescription = mapper.startDescription,
        finishDescription = mapper.finishDescription,
        orderApplication = mapper.orderApplication,
        passengerCount = mapper.passengerCount,
        maleEmployeeCount = mapper.maleEmployeeCount,
        femaleEmployeeCount = mapper.femaleEmployeeCount,
        additionalInfo = mapper.additionalInfo,
        orderTime = mapper.orderTime,
        startTime = Instant.now(),
        finishTime = null,
        absenceTime = null,
        cancelTime = null,
        createdAt = Instant.now(),
        updatedAt = null,
        deletedAt = null,
        baggage = if (mapper.baggage != null) {
            OrderBaggage(
                mapper.baggage.type,
                mapper.baggage.weight,
                mapper.baggage.isHelpNeeded
            )
        } else {
            null
        },
        startStation = startStation,
        finishStation = finishStation,
        orderStatus = OrderStatus(OrderStatusType.REVIEW, "В рассмотрении"),
        passenger = passenger,
        passengerCategory = mapper.passengerCategory ?: passenger.category.code,
        transfers = mapper.transfers.map { metroStationTransferMapper.dtoToDomain(it) },
        duration = Duration.ofSeconds(mapper.duration),
        employees = emptySet()
    )

    fun dtoToDomain(mapper: UpdatedPassengerOrderDTO, createdAt: Instant, id: Long,
                    startStation: MetroStation, finishStation: MetroStation, passenger: Passenger) = PassengerOrder(
        id = id,
        startDescription = mapper.startDescription,
        finishDescription = mapper.finishDescription,
        orderApplication = mapper.orderApplication,
        passengerCount = mapper.passengerCount,
        maleEmployeeCount = mapper.maleEmployeeCount,
        femaleEmployeeCount = mapper.femaleEmployeeCount,
        additionalInfo = mapper.additionalInfo,
        orderTime = mapper.orderTime,
        startTime = null,
        finishTime = null,
        absenceTime = null,
        cancelTime = null,
        createdAt = createdAt,
        updatedAt = Instant.now(),
        deletedAt = null,
        baggage = if (mapper.baggage != null) {
            OrderBaggage(
                mapper.baggage.type,
                mapper.baggage.weight,
                mapper.baggage.isHelpNeeded
            )
        } else {
            null
        },
        startStation = startStation,
        finishStation = finishStation,
        orderStatus = OrderStatus(OrderStatusType.ACCEPTED, "Принята"),
        passenger = passenger,
        passengerCategory = mapper.passengerCategory,
        transfers = mapper.transfers.map { metroStationTransferMapper.dtoToDomain(it) },
        duration = Duration.ofSeconds(mapper.duration),
        employees = emptySet()
    )

    fun domainToEntity(
        mapper: PassengerOrder, orderStatusEntity: OrderStatusEntity, passengerEntity: PassengerEntity,
        startStation: MetroStationEntity, finishStation: MetroStationEntity
    ) = PassengerOrderEntity(
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
        startStation = startStation,
        finishStation = finishStation,
        baggage = mapper.baggage,
        orderStatusCode = orderStatusEntity,
        passenger = passengerEntity,
        passengerCategory = passengerEntity.category,
        transfers = mapper.transfers,
        duration = mapper.duration,
        orderApplication = if (mapper.orderApplication != null) mapper.orderApplication.name else null,
    )
}