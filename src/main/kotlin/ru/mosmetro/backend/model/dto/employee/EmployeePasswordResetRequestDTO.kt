package ru.mosmetro.backend.model.dto.employee

import io.swagger.v3.oas.annotations.media.Schema

data class EmployeePasswordResetRequestDTO(
    @Schema(description = "Новый пароль")
    val newPassword: String
)
