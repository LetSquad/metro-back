package ru.mosmetro.backend.model.domain

import java.time.Duration

data class EmployeePriority(
    val employee: Employee,
    val transferTime: Duration,
    val priority: Int,
)
