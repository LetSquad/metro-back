package ru.mosmetro.backend.model.domain

data class MetroStationTransfer(
    val startStation: MetroStation,
    val finishStation: MetroStation,
    val duration: Int,
    val isCrosswalking: Boolean,
)
