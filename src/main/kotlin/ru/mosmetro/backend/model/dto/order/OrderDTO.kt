package ru.mosmetro.backend.model.dto.order

import ru.mosmetro.backend.model.dto.metro.MetroStationDTO
import ru.mosmetro.backend.model.dto.metro.MetroStationTransferDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerDTO
import ru.mosmetro.backend.model.enums.PassengerCategoryType
import java.time.LocalDateTime

data class OrderDTO(
        val id: Long,
        val startDescription: String?,
        val finishDescription: String?,
        val orderApplication: OrderApplicationDTO,
        val duration: Long,
        val passengerCount: Int,
        val maleEmployeeCount: Int,
        val femaleEmployeeCount: Int,
        val additionalInfo: String?,
        val createdTime: LocalDateTime,
        val orderTime: LocalDateTime,

        val startTime: LocalDateTime,
        val finishTime: LocalDateTime,
        val absenceTime: LocalDateTime,
        val cancelTime: LocalDateTime,
        val orderStatus: OrderStatusDTO,
        val passenger: PassengerDTO,

        val baggage: OrderBaggageDTO,
        val transfers: List<MetroStationTransferDTO>,
        val passengerCategory: PassengerCategoryType,
        val startStation: MetroStationDTO,
        val finishStation: MetroStationDTO,
)
