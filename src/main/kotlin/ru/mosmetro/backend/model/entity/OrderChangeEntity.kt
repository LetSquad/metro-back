package ru.mosmetro.backend.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "order_change")
data class OrderChangeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_change_id_gen")
    @SequenceGenerator(name = "order_change_id_gen", sequenceName = "order_change_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    val id: Long?,

    @Column(name = "order_change_code", length = Integer.MAX_VALUE)
    val orderChangeCode: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "order_change_log")
    val orderChangeLog: MutableMap<String, Any>?,

    @Column(name = "employee_login", length = Integer.MAX_VALUE)
    val employeeLogin: String?,

    @Column(name = "created_at")
    val createdAt: Instant?
)