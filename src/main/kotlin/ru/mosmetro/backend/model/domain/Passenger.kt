package ru.mosmetro.backend.model.domain

import ru.mosmetro.backend.model.enums.SexType
import java.time.Instant

data class Passenger(
    val id: Long?,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val sex: SexType,
    val comment: String?,
    val hasPacemaker: Boolean?,
    val createdAt: Instant,
    val deletedAt: Instant?,
    val category: PassengerCategory
)
