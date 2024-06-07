package ru.mosmetro.backend.model.dto.employee

import io.swagger.v3.oas.annotations.media.Schema
import ru.mosmetro.backend.model.enums.EmployeeRoleType
import ru.mosmetro.backend.model.enums.SexType

data class EmployeeDTO(
        //TODO добавить login
        @Schema(description = "Идентификатор рабочего")
        val id: Long?,
        @Schema(description = "роль")
        val employeeRole: EmployeeRoleType,
        @Schema(description = "Рабочий телефон")
        val workPhone: String,
        @Schema(description = "Личный телефон")
        val personalPhone: String,
        @Schema(description = "Имя рабочего")
        val firstName: String,
        @Schema(description = "Фамилия рабочего")
        val lastName: String,
        @Schema(description = "Отчество рабочего")
        val middleName: String?,
        @Schema(description = "Пол рабочего")
        val sex: SexType,
        @Schema(description = "смена формата hh:mm - hh:mm")
        val shift: String,
        @Schema(description = "Номер рабочего")
        val employeeNumber: Long,
        @Schema(description = "Флаг легких работ")
        val lightDuties: Boolean,
        @Schema(description = "Должность")
        val rank: EmployeeRankDTO,
        @Schema(description = "Логин пользователя")
        val login: String?,
)
