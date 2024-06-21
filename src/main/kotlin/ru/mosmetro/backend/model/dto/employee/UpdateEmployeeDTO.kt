package ru.mosmetro.backend.model.dto.employee

import io.swagger.v3.oas.annotations.media.Schema
import ru.mosmetro.backend.model.enums.SexType

data class UpdateEmployeeDTO(
    @Schema(description = "Код ранга сотрудника")
    val rank: String,
    @Schema(description = "Имя сотрудника")
    val firstName: String,
    @Schema(description = "Фамилия сотрудника")
    val lastName: String,
    @Schema(description = "Отчество сотрудника")
    val middleName: String?,
    @Schema(description = "Пол сотрудника")
    val sex: SexType,
    @Schema(description = "Рабочий телефон")
    val workPhone: String,
    @Schema(description = "Личный телефон")
    val personalPhone: String,
    @Schema(description = "Тип смены", pattern = "hh:mm-hh:mm")
    val shift: String,
    @Schema(description = "Номер сотрудника")
    val employeeNumber: Long,
    @Schema(description = "Флаг, обозначающий только легкие работы для сотрудника")
    val lightDuties: Boolean
)
