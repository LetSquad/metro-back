package ru.mosmetro.backend.model.domain

import java.time.LocalTime
import ru.mosmetro.backend.model.enums.SexType

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
    val login: String
)
