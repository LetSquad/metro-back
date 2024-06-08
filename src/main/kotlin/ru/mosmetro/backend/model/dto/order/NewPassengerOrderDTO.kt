package ru.mosmetro.backend.model.dto.order

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import ru.mosmetro.backend.model.dto.metro.MetroStationDTO
import ru.mosmetro.backend.model.dto.metro.MetroStationTransferDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerCategoryDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerDTO

data class NewPassengerOrderDTO(
    @Schema(description = "Описание старта пути заявки")
    val startDescription: String?,
    @Schema(description = "Описание финиша пути заявки")
    val finishDescription: String?,
    @Schema(description = "Способ приема заявки")
    val orderApplication: OrderApplicationDTO,
    @Schema(description = "Время заявки в секундах")
    val duration: Long,
    @Schema(description = "Количество пассажиров")
    val passengerCount: Int,
    @Schema(description = "Количество работников мужского пола, нужное для данной заявки")
    val maleEmployeeCount: Int,
    @Schema(description = "Количество работников женского пола, нужное для данной заявки")
    val femaleEmployeeCount: Int,
    @Schema(description = "Дополнительная информация")
    val additionalInfo: String?,
    @Schema(description = "Время заявки")
    val orderTime: Instant,
    @Schema(description = "Время старта заявки")
    val startTime: Instant?,
    @Schema(description = "Время окончания заявки")
    val finishTime: Instant?,
    @Schema(description = "Статус заявки")
    val orderStatus: OrderStatusDTO,
    @Schema(description = "Пассажир")
    val passenger: PassengerDTO,
    @Schema(description = "Багаж")
    val baggage: OrderBaggageDTO?,
    @Schema(description = "Пересадки в пути")
    val transfers: List<MetroStationTransferDTO>?,
    @Schema(description = "Категория пассажира")
    val passengerCategory: PassengerCategoryDTO,
    @Schema(description = "Начальная станция маршрута")
    val startMetroStation: MetroStationDTO,
    @Schema(description = "Конечная станция маршрута")
    val finishMetroStation: MetroStationDTO,
)
