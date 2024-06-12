package ru.mosmetro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.PassengerOrderEntity
import java.time.Instant

interface PassengerOrderEntityRepository : JpaRepository<PassengerOrderEntity, Long> {
    fun findAllByStartTimeBetween(dateStart: Instant, dateFinish: Instant): List<PassengerOrderEntity>
}