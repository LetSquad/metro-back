package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity
@Table(name = "employee_shift_order")
data class EmployeeShiftOrderEntity(
    @EmbeddedId
    @SequenceGenerator(name = "employee_shift_order_id_gen", sequenceName = "employee_id_seq", allocationSize = 1)
    val id: EmployeeShiftOrderEntityId?,

    @MapsId("employeeShiftId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_shift_id", nullable = false)
    val employeeShift: EmployeeShiftEntity?,

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    val order: PassengerOrderEntity?,

    @Column(name = "is_attached")
    val isAttached: Boolean?
) 