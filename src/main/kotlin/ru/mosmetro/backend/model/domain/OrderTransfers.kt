package ru.mosmetro.backend.model.domain

data class OrderTransfers(
    val duration: Long,
    val transfers: List<MetroStationTransfer>
)
