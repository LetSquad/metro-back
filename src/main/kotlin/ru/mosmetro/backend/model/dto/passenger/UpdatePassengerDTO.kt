package ru.mosmetro.backend.model.dto.passenger

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import ru.mosmetro.backend.model.enums.PassengerCategoryType
import ru.mosmetro.backend.model.enums.SexType

data class UpdatePassengerDTO(
    @Schema(description = "Имя пассажира")
    val firstName: String,
    @Schema(description = "Фамилия пассажира")
    val lastName: String,
    @Schema(description = "Отчество пассажира")
    val middleName: String?,
    @Schema(description = "Пол пассажира")
    val sex: SexType,
    @Schema(description = "Комментарии")
    val comment: String?,
    @Schema(description = "Флаг наличия сопровождающего")
    val hasPacemaker: Boolean?,
    @Schema(description = "Время удаления пассажира")
    val deletedAt: Instant?,
    @Schema(description = "Категория пассажира")
    val category: PassengerCategoryType
)
