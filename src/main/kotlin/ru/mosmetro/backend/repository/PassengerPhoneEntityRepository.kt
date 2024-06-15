package ru.mosmetro.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.PassengerPhoneEntity

interface PassengerPhoneEntityRepository : JpaRepository<PassengerPhoneEntity, Long> {

    fun findByPassengerId(passengerId: Long): List<PassengerPhoneEntity>
}
