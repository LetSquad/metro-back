package ru.mosmetro.backend.model.domain

data class UserWithRole(
    val login: String,
    val role: String
)
