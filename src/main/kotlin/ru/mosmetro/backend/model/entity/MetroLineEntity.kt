package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(name = "metro_line")
data class MetroLineEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metro_line_id_gen")
    @SequenceGenerator(name = "metro_line_id_gen", sequenceName = "metro_line_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    val id: Long?,

    @Column(name = "name", length = Integer.MAX_VALUE)
    val name: String,

    @Column(name = "color")
    val color: String
) 