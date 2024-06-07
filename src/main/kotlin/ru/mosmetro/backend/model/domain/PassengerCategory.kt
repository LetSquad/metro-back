package ru.mosmetro.backend.model.domain

import ru.mosmetro.backend.model.enums.PassengerCategoryType

data class PassengerCategory(
    val code: PassengerCategoryType,
    val name: String,
)
