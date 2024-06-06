package ru.mosmetro.backend.model.dto.metro

import io.swagger.v3.oas.annotations.media.Schema

data class MetroStationDTO(
    @Schema(description = "Идентификатор")
    val id: Long?,
    @Schema(description = "Наименование станции")
    val name: String?,
    @Schema(description = "Линия метро")
    val line: MetroLineDTO?,
)
