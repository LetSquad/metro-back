package ru.mosmetro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.EmployeeShiftEntity
import java.time.Instant

interface EmployeeShiftEntityRepository : JpaRepository<EmployeeShiftEntity, Long> {
    fun findAllByShiftDate(date: Instant): List<EmployeeShiftEntity>
}