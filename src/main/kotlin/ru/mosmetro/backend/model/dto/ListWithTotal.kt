package ru.mosmetro.backend.model.dto

data class ListWithTotal<T>(
    val total: Int,
    val list: List<T>
)
