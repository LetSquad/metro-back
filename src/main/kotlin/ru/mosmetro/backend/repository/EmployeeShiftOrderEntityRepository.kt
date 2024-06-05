package ru.mosmetro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.EmployeeShiftOrderEntity
import ru.mosmetro.backend.model.entity.EmployeeShiftOrderEntityId

interface EmployeeShiftOrderEntityRepository : JpaRepository<EmployeeShiftOrderEntity, EmployeeShiftOrderEntityId> {
}