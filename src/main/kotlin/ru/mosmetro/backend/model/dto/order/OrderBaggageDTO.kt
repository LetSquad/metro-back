package ru.mosmetro.backend.model.dto.order

import io.swagger.v3.oas.annotations.media.Schema

data class OrderBaggageDTO(
    @Schema(description = "Тип багажа")
    val type: String,
    @Schema(description = "Вес багажа")
    val weight: Int,
    @Schema(description = "Нужна ли помощь с багажом для пассажира")
    val isHelpNeeded: Boolean
)
