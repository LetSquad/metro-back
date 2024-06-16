package ru.mosmetro.backend.model.dto.order

import io.swagger.v3.oas.annotations.media.Schema
import ru.mosmetro.backend.model.enums.OrderApplicationType

data class OrderApplicationDTO(
    @Schema(description = "Источник приёма заявки")
    val code: OrderApplicationType,
    @Schema(description = "Какой-то параметр")
    val name: String,
)
