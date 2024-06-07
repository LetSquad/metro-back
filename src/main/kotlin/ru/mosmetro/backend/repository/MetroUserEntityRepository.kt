package ru.mosmetro.backend.repository;

import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.MetroUserEntity

interface MetroUserEntityRepository : JpaRepository<MetroUserEntity, Long> {
    fun findByLogin(login: String): Optional<MetroUserEntity>
}