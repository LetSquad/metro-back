package ru.mosmetro.backend.model.domain

data class OrderBaggage(
    val type: String,
    val weight: Int,
    val isHelpNeeded: Boolean
)
