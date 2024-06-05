package ru.mosmetro.backend.model.dto.employee

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalTime

data class EmployeeShiftDTO(
        @Schema(description = "Идентификатор времени рабочего")
        val id: Long?,
        @Schema(description = "Имя рабочего")
        val shiftDate: Instant?,
        @Schema(description = "Фамилия рабочего")
        val workStart: LocalTime?,
        @Schema(description = "Время конца работы")
        val workFinish: LocalTime?,
)
