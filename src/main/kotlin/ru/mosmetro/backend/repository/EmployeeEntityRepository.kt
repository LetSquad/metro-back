package ru.mosmetro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.EmployeeEntity

interface EmployeeEntityRepository : JpaRepository<EmployeeEntity, Long> {

    fun findByUserLogin(userLogin: String): EmployeeEntity
}
