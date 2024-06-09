package ru.mosmetro.backend.model.dto.order

import ru.mosmetro.backend.model.enums.TimeListActionType
import java.time.LocalTime

data class EmployeeOrderActionDTO(
        val timeStart: LocalTime,
        val timeEnd: LocalTime,
        val actionType: TimeListActionType,
        val order: OrderDTO,
)
