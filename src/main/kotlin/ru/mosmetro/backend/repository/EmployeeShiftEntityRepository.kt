package ru.mosmetro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.EmployeeShiftEntity
import java.time.Instant
import java.util.Optional

interface EmployeeShiftEntityRepository : JpaRepository<EmployeeShiftEntity, Long> {
    fun findAllByShiftDate(date: Instant): List<EmployeeShiftEntity>
    fun findByShiftDateBetweenAndEmployeeId(dateStart: Instant, dateFinish: Instant, employeeId: Long): Optional<EmployeeShiftEntity>
    fun findByShiftDateAndEmployeeId(date: Instant, employeeId: Long): Optional<EmployeeShiftEntity>
}