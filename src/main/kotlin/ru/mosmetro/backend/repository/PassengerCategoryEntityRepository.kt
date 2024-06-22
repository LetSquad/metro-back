package ru.mosmetro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.PassengerCategoryEntity
import java.util.Optional

interface PassengerCategoryEntityRepository : JpaRepository<PassengerCategoryEntity, String> {
    fun findByCode(code: String): Optional<PassengerCategoryEntity>
}