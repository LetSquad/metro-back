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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ru.mosmetro.backend.model.domain.MetroStationTransfer
import ru.mosmetro.backend.model.domain.OrderBaggage
import java.time.Duration
import java.time.Instant

@Entity
@Table(name = "passenger_order")
data class PassengerOrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "passenger_order_id_gen")
    @SequenceGenerator(name = "passenger_order_id_gen", sequenceName = "passenger_order_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    val id: Long?,

    @Column(name = "start_description", length = Integer.MAX_VALUE)
    val startDescription: String?,

    @Column(name = "finish_description", length = Integer.MAX_VALUE)
    val finishDescription: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transfers")
    val transfers: List<MetroStationTransfer>,

    @Column(name = "passenger_count")
    val passengerCount: Int,

    @Column(name = "male_employee_count")
    val maleEmployeeCount: Int,

    @Column(name = "female_employee_count")
    val femaleEmployeeCount: Int,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "baggage")
    val baggage: OrderBaggage?,

    @Column(name = "additional_info", length = Integer.MAX_VALUE)
    val additionalInfo: String?,

    @Column(name = "order_time")
    val orderTime: Instant,

    @Column(name = "start_time")
    val startTime: Instant?,

    @Column(name = "finish_time")
    val finishTime: Instant?,

    @Column(name = "absence_time")
    val absenceTime: Instant?,

    @Column(name = "cancel_time")
    val cancelTime: Instant?,

    @Column(name = "created_at")
    val createdAt: Instant,

    @Column(name = "updated_at")
    val updatedAt: Instant?,

    @Column(name = "deleted_at")
    val deletedAt: Instant?,

    @Column(name = "order_application")
    val orderApplication: String?,

    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    @Column(name = "duration", columnDefinition = "interval")
    val duration: Duration,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_status_code")
    val orderStatusCode: OrderStatusEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    val passenger: PassengerEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_category")
    val passengerCategory: PassengerCategoryEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_station_id")
    val startStation: MetroStationEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finish_station_id")
    val finishStation: MetroStationEntity,
)