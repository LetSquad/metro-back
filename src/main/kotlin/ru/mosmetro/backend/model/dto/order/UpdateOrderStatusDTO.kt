package ru.mosmetro.backend.model.dto.order

import io.swagger.v3.oas.annotations.media.Schema

data class UpdateOrderStatusDTO(
    @Schema(description = "Код статуса заявки")
    val status: String,
)
