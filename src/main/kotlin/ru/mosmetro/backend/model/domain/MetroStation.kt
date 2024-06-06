package ru.mosmetro.backend.model.domain

data class MetroStation(
    val id: Long?,
    val name: String?,
    val line: MetroLine?,
)
