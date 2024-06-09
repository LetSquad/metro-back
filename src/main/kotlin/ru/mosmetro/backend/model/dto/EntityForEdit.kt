package ru.mosmetro.backend.model.dto

import io.swagger.v3.oas.annotations.media.Schema

data class EntityForEdit<T>(

    @Schema(description = "Редактирует ли сущность кто-то другой в этот момент")
    val isLockedForEdit: Boolean,

    @Schema(description = "Сущность для редактирования")
    val data: T
)
