package ru.mosmetro.backend.model.dto.passenger

import io.swagger.v3.oas.annotations.media.Schema

data class PassengerCategoryDTO(
    @Schema(description = "Код категории")
    val code: String?,
    @Schema(description = "Наименование")
    val name: String?,
)
