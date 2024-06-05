package ru.mosmetro.backend.model.domain

import java.time.Instant
import java.time.LocalTime

data class EmployeeShift(
        val id: Long?,
        val shiftDate: Instant?,
        val workStart: LocalTime?,
        val workFinish: LocalTime?,
)
