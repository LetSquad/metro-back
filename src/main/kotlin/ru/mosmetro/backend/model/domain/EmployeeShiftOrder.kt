package ru.mosmetro.backend.model.domain

import ru.mosmetro.backend.model.enums.TimeListActionType
import java.time.LocalDateTime

data class EmployeeShiftOrder(
        val timeStart: LocalDateTime,
        val timeFinish: LocalDateTime,
        val actionType: TimeListActionType,
        val order: PassengerOrder?,
)
