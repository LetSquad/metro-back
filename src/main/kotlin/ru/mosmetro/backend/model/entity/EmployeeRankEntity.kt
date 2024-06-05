package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(name = "employee_rank")
data class EmployeeRankEntity(
    @Id
    @SequenceGenerator(name = "employee_rank_id_gen", sequenceName = "employee_id_seq", allocationSize = 1)
    @Column(name = "code", nullable = false, length = Integer.MAX_VALUE)
    val code: String?,

    @Column(name = "name", length = Integer.MAX_VALUE)
    val name: String?,

    @Column(name = "short_name", length = Integer.MAX_VALUE)
    val shortName: String?,

    @Column(name = "role", length = Integer.MAX_VALUE)
    val role: String?
) 