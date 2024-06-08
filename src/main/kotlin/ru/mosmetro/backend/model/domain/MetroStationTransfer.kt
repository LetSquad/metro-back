package ru.mosmetro.backend.model.domain

import java.time.Duration

data class MetroStationTransfer(
    val startStation: MetroStation,
    val finishStation: MetroStation,
    val duration: Duration,
    val isCrosswalking: Boolean,
)
