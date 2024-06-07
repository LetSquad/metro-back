package ru.mosmetro.backend.model.dto.employee

import io.swagger.v3.oas.annotations.media.Schema
import ru.mosmetro.backend.model.enums.EmployeeRoleType

data class EmployeeRankDTO(
    @Schema(description = "Код должности")
    val code: String,
    @Schema(description = "Наименование")
    val name: String,
    @Schema(description = "Сокращенное название")
    val shortName: String,
    @Schema(description = "Роль к должности")
    val role: EmployeeRoleType,
)
