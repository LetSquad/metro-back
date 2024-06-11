package ru.mosmetro.backend.model.dto.order

import ru.mosmetro.backend.model.enums.TimeListActionType
import java.time.LocalDateTime

data class EmployeeShiftOrderDTO(
        val timeStart: LocalDateTime,
        val timeFinish: LocalDateTime,
        val actionType: TimeListActionType,
        val order: PassengerOrderDTO?,
)
