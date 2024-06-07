package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(name = "metro_user")
data class MetroUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metro_user_id_gen")
    @SequenceGenerator(name = "metro_user_id_gen", sequenceName = "metro_user_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    val id: Long?,

    @Column(name = "login", length = Integer.MAX_VALUE)
    val login: String,

    @Column(name = "password", length = Integer.MAX_VALUE)
    val password: String,

    @Column(name = "is_password_temporary")
    val isPasswordTemporary: Boolean
)