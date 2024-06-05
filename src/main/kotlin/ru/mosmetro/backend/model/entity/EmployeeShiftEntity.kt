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
import java.time.LocalTime

@Entity
@Table(name = "employee_shift")
data class EmployeeShiftEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_shift_id_gen")
    @SequenceGenerator(name = "employee_shift_id_gen", sequenceName = "employee_shift_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    val id: Long?,

    @Column(name = "shift_date")
    val shiftDate: Instant?,

    @Column(name = "work_start")
    val workStart: LocalTime?,

    @Column(name = "work_finish")
    val workFinish: LocalTime?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    val employee: EmployeeEntity?
) 