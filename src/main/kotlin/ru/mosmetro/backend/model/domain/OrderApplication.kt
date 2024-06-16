package ru.mosmetro.backend.model.domain

import ru.mosmetro.backend.model.enums.OrderApplicationType

data class OrderApplication(
    val code: OrderApplicationType,
    val name: String,
)
