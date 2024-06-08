package ru.mosmetro.backend.model.dto.metro

import io.swagger.v3.oas.annotations.media.Schema

data class MetroLineDTO(
    @Schema(description = "Идентификатор")
    val id: Long?,
    @Schema(description = "Наименование линии")
    val name: String,
    @Schema(description = "Цвет линии")
    val color: String,
)
