package ru.mosmetro.backend.model.dto.employee

import io.swagger.v3.oas.annotations.media.Schema
import ru.mosmetro.backend.model.enums.SexType
import java.time.LocalTime

data class UpdateEmployeeDTO(
    @Schema(description = "Имя рабочего")
    val firstName: String,
    @Schema(description = "Фамилия рабочего")
    val lastName: String,
    @Schema(description = "Отчество рабочего")
    val middleName: String?,
    @Schema(description = "Пол рабочего")
    val sex: SexType,
    @Schema(description = "Время старта работы")
    val workStart: LocalTime,
    @Schema(description = "Время конца работы")
    val workFinish: LocalTime,
    @Schema(description = "Тип смены")
    val shiftType: String,
    @Schema(description = "Рабочий телефон")
    val workPhone: String,
    @Schema(description = "Личный телефон")
    val personalPhone: String,
    @Schema(description = "Номер рабочего")
    val employeeNumber: Long,
    @Schema(description = "Флаг легких работ")
    val lightDuties: Boolean,
)
