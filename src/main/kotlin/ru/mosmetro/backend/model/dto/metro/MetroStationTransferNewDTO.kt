package ru.mosmetro.backend.model.dto.metro

import io.swagger.v3.oas.annotations.media.Schema

data class MetroStationTransferNewDTO(
    @Schema(description = "Идентификатор начальной станция маршрута")
    val startStation: Long,
    @Schema(description = "Идентификатор конечной станция маршрута")
    val finishStation: Long,
    @Schema(description = "Время маршрута")
    val duration: Long,
    @Schema(description = "Флаг пешего маршрута")
    val isCrosswalking: Boolean,
)
