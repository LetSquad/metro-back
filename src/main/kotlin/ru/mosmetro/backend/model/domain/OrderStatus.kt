package ru.mosmetro.backend.model.domain

import ru.mosmetro.backend.model.enums.OrderStatusType

data class OrderStatus(
    val code: OrderStatusType,
    val name: String,
)
