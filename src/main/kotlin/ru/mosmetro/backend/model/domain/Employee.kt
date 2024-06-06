package ru.mosmetro.backend.model.domain

import ru.mosmetro.backend.model.enums.SexType
import java.time.LocalTime

data class Employee(
    val id: Long?,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val sex: SexType,
    val workStart: LocalTime,
    val workFinish: LocalTime,
    val shiftType: String,
    val workPhone: String,
    val personalPhone: String,
    val employeeNumber: Long,
    val lightDuties: Boolean,
    val rank: EmployeeRank,
)
