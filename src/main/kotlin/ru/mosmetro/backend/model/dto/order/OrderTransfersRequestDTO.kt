package ru.mosmetro.backend.model.dto.order

data class OrderTransfersRequestDTO(
    val startStation: Long,
    val finishStation: Long
)
