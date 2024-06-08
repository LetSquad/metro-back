package ru.mosmetro.backend.model.dto.metro

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Duration

data class MetroStationTransferDTO(
    @Schema(description = "Начальная станция маршрута")
    val startStation: MetroStationDTO,
    @Schema(description = "Конечная станция маршрута")
    val finishStation: MetroStationDTO,
    @Schema(description = "Время маршрута")
    val duration: Duration,
    @Schema(description = "Флаг пешего маршрута")
    val isCrosswalking: Boolean,
)
