package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(name = "metro_station")
data class MetroStationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metro_station_id_gen")
    @SequenceGenerator(name = "metro_station_id_gen", sequenceName = "metro_station_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    val id: Long?,

    @Column(name = "name", length = Integer.MAX_VALUE)
    val name: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    val line: MetroLineEntity?
)