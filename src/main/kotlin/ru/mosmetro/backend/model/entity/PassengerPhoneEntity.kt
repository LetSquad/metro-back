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
@Table(name = "passenger_phone")
data class PassengerPhoneEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "passenger_phone_id_gen")
    @SequenceGenerator(name = "passenger_phone_id_gen", sequenceName = "passenger_phone_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    val id: Long?,

    @Column(name = "phone_number", length = Integer.MAX_VALUE)
    val phoneNumber: String,

    @Column(name = "description", length = Integer.MAX_VALUE)
    val description: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    val passenger: PassengerEntity?
) {

    @Column(name = "passenger_id", insertable = false, updatable = false)
    var passengerId: Long? = null
}
