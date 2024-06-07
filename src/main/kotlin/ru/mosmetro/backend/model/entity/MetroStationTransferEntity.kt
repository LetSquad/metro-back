package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(name = "metro_station_transfer")
data class MetroStationTransferEntity(
    @EmbeddedId
    @SequenceGenerator(name = "metro_station_transfer_id_gen", sequenceName = "employee_id_seq", allocationSize = 1)
    val id: MetroStationTransferEntityId?,

    @MapsId("startStationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "start_station_id", nullable = false)
    val startStation: MetroStationEntity?,

    @MapsId("finishStationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finish_station_id", nullable = false)
    val finishStation: MetroStationEntity?,

    @Column(name = "is_crosswalking")
    val isCrosswalking: Boolean,

    @Column(name = "duration")
    val duration: Int
)