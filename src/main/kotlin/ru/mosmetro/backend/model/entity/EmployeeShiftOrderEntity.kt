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
@Table(name = "employee_shift_order")
data class EmployeeShiftOrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_shift_order_id_gen")
    @SequenceGenerator(name = "employee_shift_order_id_gen", sequenceName = "employee_shift_order_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    val id: Long?,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_shift_id", nullable = false)
    val employeeShift: EmployeeShiftEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    val order: PassengerOrderEntity?,

    @Column(name = "is_attached")
    val isAttached: Boolean,

    @Column(name = "action_type")
    val actionType: String,

    @Column(name = "time_start")
    val timeStart: Instant,

    @Column(name = "time_finish")
    val timeFinish: Instant,

    @Column(name = "created_at")
    val createdAt: Instant,
)