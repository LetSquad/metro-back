package ru.mosmetro.backend.model.dto.employee

data class EmployeeFilterRequestDTO(
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val lightDuties: Boolean?,
)
