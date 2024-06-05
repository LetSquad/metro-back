package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotNull
import java.io.Serializable
import java.util.Objects
import org.hibernate.Hibernate

@Embeddable
open class EmployeeShiftOrderEntityId : Serializable {
    @NotNull
    @Column(name = "employee_shift_id", nullable = false)
    open var employeeShiftId: Long? = null

    @NotNull
    @Column(name = "order_id", nullable = false)
    open var orderId: Long? = null
    override fun hashCode(): Int = Objects.hash(employeeShiftId, orderId)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as EmployeeShiftOrderEntityId

        return employeeShiftId == other.employeeShiftId &&
                orderId == other.orderId
    }

    companion object {
        private const val serialVersionUID = 4790190228541814157L
    }
}