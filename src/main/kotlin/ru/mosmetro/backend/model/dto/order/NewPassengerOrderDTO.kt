package ru.mosmetro.backend.model.dto.order

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class NewPassengerOrderDTO(
    @Schema(description = "Описание старта пути заявки")
    val startDescription: String?,
    @Schema(description = "Описание финиша пути заявки")
    val finishDescription: String?,
    @Schema(description = "Порядок чего-то")
    val orderApplication: String?,
    @Schema(description = "Количество пассажиров")
    val passengerCount: Int?,
    @Schema(description = "Количество работников мужского пола, нужное для данной заявки")
    val maleEmployeeCount: Int?,
    @Schema(description = "Количество работников женского пола, нужное для данной заявки")
    val femaleEmployeeCount: Int?,
    @Schema(description = "Дополнительная информация")
    val additionalInfo: String?,
    @Schema(description = "Время заявки")
    val orderTime: Instant?,
    @Schema(description = "Время старта заявки")
    val startTime: Instant?,
    @Schema(description = "Время окончания заявки")
    val finishTime: Instant?
)
