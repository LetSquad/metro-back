package ru.mosmetro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.OrderChangeEntity

interface OrderChangeEntityRepository : JpaRepository<OrderChangeEntity, Long> {
}