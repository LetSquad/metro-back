package ru.mosmetro.backend.model.dto.order

import io.swagger.v3.oas.annotations.media.Schema
import ru.mosmetro.backend.model.enums.OrderStatusType

data class OrderStatusDTO(
    @Schema(description = "Код статуса заявки")
    val code: OrderStatusType,
    @Schema(description = "Название статуса")
    val name: String,
)
