package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(name = "passenger_category")
data class PassengerCategoryEntity(

    @Id
    @SequenceGenerator(name = "passenger_category_id_gen", sequenceName = "employee_id_seq", allocationSize = 1)
    @Column(name = "code", nullable = false, length = Integer.MAX_VALUE)
    val code: String,

    @Column(name = "name", length = Integer.MAX_VALUE)
    val name: String,

    @Column(name = "short_name", length = Integer.MAX_VALUE)
    val shortName: String
)
