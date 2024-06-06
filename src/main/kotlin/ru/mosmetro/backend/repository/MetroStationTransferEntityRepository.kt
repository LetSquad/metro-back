package ru.mosmetro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository
import ru.mosmetro.backend.model.entity.MetroStationTransferEntity
import ru.mosmetro.backend.model.entity.MetroStationTransferEntityId

interface MetroStationTransferEntityRepository :
    JpaRepository<MetroStationTransferEntity, MetroStationTransferEntityId> {
}