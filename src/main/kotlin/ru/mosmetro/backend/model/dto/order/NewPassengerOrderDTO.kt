package ru.mosmetro.backend.model.dto.order

import io.swagger.v3.oas.annotations.media.Schema
import ru.mosmetro.backend.model.dto.metro.MetroStationDTO
import ru.mosmetro.backend.model.dto.metro.MetroStationTransferDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerCategoryDTO
import ru.mosmetro.backend.model.dto.passenger.PassengerDTO
import ru.mosmetro.backend.model.enums.OrderApplicationType
import ru.mosmetro.backend.model.enums.PassengerCategoryType
import java.time.Instant

data class NewPassengerOrderDTO(
    @Schema(description = "Описание старта пути заявки")
    val startDescription: String?,
    @Schema(description = "Описание финиша пути заявки")
    val finishDescription: String?,
    @Schema(description = "Способ приема заявки")
    val orderApplication: OrderApplicationType?,
    @Schema(description = "Количество пассажиров")
    val passengerCount: Int,
    @Schema(description = "Дополнительная информация")
    val additionalInfo: String?,
    @Schema(description = "Время заявки")
    val orderTime: Instant,
    @Schema(description = "Идентификатор пассажира")
    val passenger: Long,
    @Schema(description = "Количество работников мужского пола, нужное для данной заявки")
    val maleEmployeeCount: Int,
    @Schema(description = "Количество работников женского пола, нужное для данной заявки")
    val femaleEmployeeCount: Int,
    @Schema(description = "Багаж")
    val baggage: OrderBaggageDTO?,
    @Schema(description = "Категория пассажира")
    val passengerCategory: PassengerCategoryType,
    @Schema(description = "Идентификатор начальной станция маршрута")
    val startStation: Long,
    @Schema(description = "Идентификатор конечной станция маршрута")
    val finishStation: Long,
    @Schema(description = "Пересадки в пути")
    val transfers: List<MetroStationTransferDTO>,
    @Schema(description = "Время заявки в секундах")
    val duration: Long
)
