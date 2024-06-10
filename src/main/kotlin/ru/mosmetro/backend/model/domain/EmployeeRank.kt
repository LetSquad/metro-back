package ru.mosmetro.backend.model.domain

data class EmployeeRank(
    val code: String,
    val name: String,
    val shortName: String?,
    val role: String,
)
