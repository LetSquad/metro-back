package ru.mosmetro.backend.model.entity

import jakarta.persistence.*
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
    val personalPhone: String?,

    @Column(name = "employee_number")
    val employeeNumber: Long?,

    @Column(name = "light_duties")
    val lightDuties: Boolean?
)