package ru.mosmetro.backend.model.domain

import java.time.Duration
import java.time.Instant

data class PassengerOrder(
    val id: Long?,
    val startDescription: String?,
    val finishDescription: String?,
    val orderApplication: OrderApplication,
    val passengerCount: Int,
    val maleEmployeeCount: Int,
    val femaleEmployeeCount: Int,
    val duration: Duration,
    val additionalInfo: String?,
    val orderTime: Instant,
    val startTime: Instant?,
    val finishTime: Instant?,
    val absenceTime: Instant?,
    val cancelTime: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val deletedAt: Instant?,
    val orderStatus: OrderStatus,
    val passenger: Passenger,
    val baggage: OrderBaggage?,
    val transfers: List<MetroStationTransfer>,
    val passengerCategory: PassengerCategory,
    val startMetroStation: MetroStation,
    val finishMetroStation: MetroStation,
)
