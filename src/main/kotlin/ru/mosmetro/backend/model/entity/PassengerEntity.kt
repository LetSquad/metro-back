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
import java.time.Instant

@Entity
@Table(name = "passenger")
data class PassengerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "passenger_id_gen")
    @SequenceGenerator(name = "passenger_id_gen", sequenceName = "passenger_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    val id: Long?,

    @Column(name = "first_name", length = Integer.MAX_VALUE)
    val firstName: String,

    @Column(name = "last_name", length = Integer.MAX_VALUE)
    val lastName: String,

    @Column(name = "middle_name", length = Integer.MAX_VALUE)
    val middleName: String?,

    @Column(name = "sex", length = Integer.MAX_VALUE)
    val sex: String,

    @Column(name = "comment", length = Integer.MAX_VALUE)
    val comment: String?,

    @Column(name = "has_pacemaker")
    val hasPacemaker: Boolean?,

    @Column(name = "created_at")
    val createdAt: Instant,

    @Column(name = "deleted_at")
    val deletedAt: Instant?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    val category: PassengerCategoryEntity
)