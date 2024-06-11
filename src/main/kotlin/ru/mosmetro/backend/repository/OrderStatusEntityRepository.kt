package ru.mosmetro.backend.repository;

import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.OrderStatusEntity

interface OrderStatusEntityRepository : JpaRepository<OrderStatusEntity, String> {
    fun findByCode(code: String): Optional<OrderStatusEntity>
}