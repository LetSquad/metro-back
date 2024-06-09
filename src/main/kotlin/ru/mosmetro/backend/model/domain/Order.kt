package ru.mosmetro.backend.model.domain

import ru.mosmetro.backend.model.dto.passenger.PassengerDTO
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.model.enums.PassengerCategoryType
import java.time.LocalDateTime

data class Order(
        val id: Long,
        val startDescription: String?,
        val finishDescription: String?,
        val orderApplication: OrderApplication?,
        val duration: Long,
        val passengerCount: Int,
        val maleEmployeeCount: Int,
        val femaleEmployeeCount: Int,
        val additionalInfo: String?,
        val createdTime: LocalDateTime,
        val orderTime: LocalDateTime,

        val startTime: LocalDateTime,
        val finishTime: LocalDateTime,
        val absenceTime: LocalDateTime?,
        val cancelTime: LocalDateTime?,
        val orderStatus: OrderStatusType,
        val passenger: PassengerDTO?,

        val baggage: OrderBaggage?,
        val transfers: List<MetroStationTransfer>,
        val passengerCategory: PassengerCategoryType?,
        val startStation: MetroStation,
        val finishStation: MetroStation,
)
