package ru.mosmetro.backend.model.domain

import ru.mosmetro.backend.model.enums.PassengerCategoryType
import ru.mosmetro.backend.util.MetroTimeUtil.BEFORE_ORDER_TIME_PERIOD_SEC
import java.time.Duration
import java.time.Instant

data class PassengerOrder(
    val id: Long?,
    val startDescription: String?,
    val finishDescription: String?,
    val orderApplication: OrderApplication,
    val passengerCount: Int,
    val maleEmployeeCount: Int,
    val femaleEmployeeCount: Int,
    val duration: Duration,
    val additionalInfo: String?,
    val orderTime: Instant,
    val startTime: Instant?,
    val finishTime: Instant?,
    val absenceTime: Instant?,
    val cancelTime: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val deletedAt: Instant?,
    val orderStatus: OrderStatus,
    val passenger: Passenger,
    val baggage: OrderBaggage?,
    val transfers: List<MetroStationTransfer>,
    val passengerCategory: PassengerCategoryType,
    val startStation: MetroStation,
    val finishStation: MetroStation,
    val employees: Set<Employee>?
) {
    fun getSupposedFinishTime(
    ): Instant {
        return this.orderTime.plusSeconds(this.duration.toSeconds())
    }

    fun getOrderTime(
        addPeriodBefore: Boolean
    ): Instant {
        return if (addPeriodBefore) this.orderTime.minusSeconds(BEFORE_ORDER_TIME_PERIOD_SEC) else this.orderTime
    }
}
