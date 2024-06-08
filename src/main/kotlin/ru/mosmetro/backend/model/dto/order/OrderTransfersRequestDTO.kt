package ru.mosmetro.backend.model.dto.order

data class OrderTransfersRequestDTO(
    val startStationId: Long,
    val finishStationId: Long
)
