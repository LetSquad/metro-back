package ru.mosmetro.backend.model.dto.order

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import ru.mosmetro.backend.model.dto.metro.MetroStationTransferNewDTO
import ru.mosmetro.backend.model.enums.OrderApplicationType
import ru.mosmetro.backend.model.enums.PassengerCategoryType

data class UpdatedPassengerOrderDTO(
    @Schema(description = "Описание старта пути заявки")
    val startDescription: String?,
    @Schema(description = "Описание финиша пути заявки")
    val finishDescription: String?,
    @Schema(description = "Способ приема заявки")
    val orderApplication: OrderApplicationType,
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
    @Schema(description = "Сотрудники, прикрепленные к заявке")
    val employees: Set<Long>?,
    @Schema(description = "Категория пассажира")
    val passengerCategory: PassengerCategoryType,
    @Schema(description = "Идентификатор начальной станции маршрута")
    val startStation: Long,
    @Schema(description = "Идентификатор конечной станции маршрута")
    val finishStation: Long,
    @Schema(description = "Пересадки в пути")
    val transfers: List<MetroStationTransferNewDTO>,
    @Schema(description = "Время заявки в секундах")
    val duration: Long
)
