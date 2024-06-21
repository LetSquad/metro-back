package ru.mosmetro.backend.model.dto.passenger

data class PassangerFilterRequestDTO(
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val categories: String?,
)