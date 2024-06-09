package ru.mosmetro.backend.model.domain.combi

import ru.mosmetro.backend.model.domain.Employee
import java.time.Duration

data class EmployeePriority(
        val employee: Employee,
        val transferTime: Duration,
        val priority: Int,
)
