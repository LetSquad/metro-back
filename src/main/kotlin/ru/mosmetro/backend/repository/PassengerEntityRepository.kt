package ru.mosmetro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.PassengerEntity

interface PassengerEntityRepository : JpaRepository<PassengerEntity, Long> {
}