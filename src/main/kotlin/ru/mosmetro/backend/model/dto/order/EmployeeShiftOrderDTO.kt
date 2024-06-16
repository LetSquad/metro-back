package ru.mosmetro.backend.model.dto.order

import ru.mosmetro.backend.model.enums.TimeListActionType
import java.time.Instant

data class EmployeeShiftOrderDTO(
        val timeStart: Instant,
        val timeEnd: Instant,
        val actionType: TimeListActionType,
        val order: PassengerOrderDTO?,
)
