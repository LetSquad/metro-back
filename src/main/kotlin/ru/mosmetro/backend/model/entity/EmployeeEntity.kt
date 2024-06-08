package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import java.time.LocalTime

@Entity
@Table(name = "employee")
data class EmployeeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_id_gen")
    @SequenceGenerator(name = "employee_id_gen", sequenceName = "employee_id_seq", allocationSize = 1)
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

    @Column(name = "work_start")
    val workStart: LocalTime,

    @Column(name = "work_finish")
    val workFinish: LocalTime,

    @Column(name = "shift_type", length = Integer.MAX_VALUE)
    val shiftType: String,

    @Column(name = "work_phone", length = Integer.MAX_VALUE)
    val workPhone: String,

    @Column(name = "personal_phone", length = Integer.MAX_VALUE)
    val personalPhone: String,

    @Column(name = "employee_number")
    val employeeNumber: Long,

    @Column(name = "light_duties")
    val lightDuties: Boolean,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rank_code")
    val rank: EmployeeRankEntity,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: MetroUserEntity
)