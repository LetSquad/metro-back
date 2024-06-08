package ru.mosmetro.backend.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Duration
import java.time.Instant
import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.MetroStationTransfer
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
import ru.mosmetro.backend.util.toPGObject

@Component
class OrderMapper(
    private val metroStationMapper: MetroStationMapper,
    private val metroStationTransferMapper: MetroStationTransferMapper,
    private val passengerMapper: PassengerMapper,
    private val passengerCategoryMapper: PassengerCategoryMapper,
    private val gson: Gson
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
        baggage = gson.fromJson(
            mapper.baggage?.value,
            object : TypeToken<Collection<OrderBaggage?>?>() {}.type
        ),
        startMetroStation = metroStationMapper.entityToDomain(mapper.startStation),
        finishMetroStation = metroStationMapper.entityToDomain(mapper.finishStation),
        orderStatus = OrderStatus(OrderStatusType.valueOf(mapper.orderStatusCode.code), mapper.orderStatusCode.name),
        passenger = passengerMapper.entityToDomain(mapper.passenger),
        passengerCategory = passengerCategoryMapper.entityToDomain(mapper.passengerCategory),
        transfers = gson.fromJson(
            mapper.baggage?.value ?: "[]",
            object : TypeToken<Collection<MetroStationTransfer?>?>() {}.type
        ),
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
        transfers = if (mapper.transfers != null) mapper.transfers.map {
            metroStationTransferMapper.domainToDto(
                it
            )
        } else null
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
        transfers = if (mapper.transfers != null) mapper.transfers.map {
            metroStationTransferMapper.dtoToDomain(
                it
            )
        } else null,
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
        transfers = if (mapper.transfers != null) mapper.transfers.map {
            metroStationTransferMapper.dtoToDomain(
                it
            )
        } else null,
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
        baggage = gson.toJson(mapper.baggage).toPGObject(),
        orderStatusCode = OrderStatusEntity(mapper.orderApplication.code, mapper.orderStatus.name),
        passenger = passengerMapper.domainToEntity(mapper.passenger, mapper.passenger.category.name),
        passengerCategory = passengerCategoryMapper.domainToEntity(mapper.passengerCategory),
        transfers = gson.toJson(mapper.transfers).toPGObject(),
        duration = mapper.duration
    )
}