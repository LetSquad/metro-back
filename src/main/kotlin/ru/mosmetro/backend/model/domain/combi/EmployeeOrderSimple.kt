package ru.mosmetro.backend.model.domain.combi

data class EmployeeOrderSimple(
        val order: OrderSimple,
        val employee: List<EmployeePriority>,
)
