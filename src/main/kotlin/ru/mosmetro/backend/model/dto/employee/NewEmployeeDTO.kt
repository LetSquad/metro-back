package ru.mosmetro.backend.model.dto.employee

import ru.mosmetro.backend.model.enums.EmployeeRoleType
import ru.mosmetro.backend.model.enums.SexType

data class NewEmployeeDTO(
        val employeeRole: EmployeeRoleType,
        val workPhone: String,
        val personalPhone: String,
        val firstName: String,
        val lastName: String,
        val middleName: String?,
        val sex: SexType,
        val shift: String,
        val employeeNumber: Long,
        val lightDuties: Boolean,
        val rankCode: String,
)
