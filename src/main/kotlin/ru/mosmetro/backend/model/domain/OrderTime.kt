package ru.mosmetro.backend.model.domain

data class OrderTime(
    val employee: Employee,
    val timePlan: MutableList<EmployeeShiftOrder>,
)
