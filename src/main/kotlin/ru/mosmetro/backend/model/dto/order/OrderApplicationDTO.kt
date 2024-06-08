package ru.mosmetro.backend.model.dto.order

import io.swagger.v3.oas.annotations.media.Schema

data class OrderApplicationDTO(
    @Schema(description = "Источник приёма заявки")
    val code: String,
    @Schema(description = "Какой-то параметр")
    val name: String,
)
