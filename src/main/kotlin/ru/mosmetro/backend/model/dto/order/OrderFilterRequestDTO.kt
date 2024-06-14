package ru.mosmetro.backend.model.dto.order

import java.time.ZonedDateTime

data class OrderFilterRequestDTO(
    val dateFrom: ZonedDateTime,
    val dateTo: ZonedDateTime,

    val passengerFirstName: String?,
    val passengerLastName: String?,
    val passengerPhone: String?,

    val employeeFirstName: String?,
    val employeeLastName: String?,
    val employeePhone: String?,

    val orderCategories: List<String>?,
    val orderStatuses: List<String>?
)
