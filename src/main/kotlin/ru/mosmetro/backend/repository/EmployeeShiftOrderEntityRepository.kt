package ru.mosmetro.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.EmployeeShiftOrderEntity
import ru.mosmetro.backend.model.entity.EmployeeShiftOrderEntityId

interface EmployeeShiftOrderEntityRepository : JpaRepository<EmployeeShiftOrderEntity, EmployeeShiftOrderEntityId> {

    fun findAllByEmployeeShiftId(employeeShiftId: Long): List<EmployeeShiftOrderEntity>

    fun findAllByOrderId(orderId: Long): List<EmployeeShiftOrderEntity>
}
