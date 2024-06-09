package ru.mosmetro.backend.model.domain.combi

import ru.mosmetro.backend.model.domain.Employee

data class EmployeeTimePlan(
    val employee: Employee,
    val timePlan: MutableList<TimePlan>,
)
