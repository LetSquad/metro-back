package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.model.domain.BreakTimeDistribution
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.domain.EmployeeShiftOrder
import ru.mosmetro.backend.model.domain.OrderTime
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.enums.TimeListActionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@Service
class BreakTimeGuesserService {

    fun guessBreakTime(
        planDate: LocalDate,
        employeeTimePlanList: List<OrderTime>,
        passengerOrderList: List<PassengerOrder>,
        addPeriodBeforeOrder: Boolean,
    ) {
        if (allEmployeesHasBreak(employeeTimePlanList)) {
            return
        }


        // первым делом задаем обед тем сотрудником, кто работает в ночное время (когда есть время без заявок)
        employeeTimePlanList
            .filter { employeeWorkWhenMetroStop(it.employee) }
            .let {
                it.forEach {
                    it.timePlan.add(
                        EmployeeShiftOrder(
                            timeStart = LocalDateTime.of(planDate.plusDays(1), LocalTime.of(3, 0)),
                            timeFinish = LocalDateTime.of(planDate.plusDays(1), LocalTime.of(4, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null
                        )
                    )
                }
            }


        if (allEmployeesHasBreak(employeeTimePlanList)) {
            return
        }


        // создаем карту загруженности
        var start = LocalDateTime.of(planDate, LocalTime.of(6, 0))
        val finish = LocalDateTime.of(planDate.plusDays(1), LocalTime.of(0, 0))

        val breakTimeList: MutableList<BreakTimeDistribution> = mutableListOf()
        while (start < finish) {
            val ordersBetweenTime =
                passengerOrderList
                    .filter {
                        !(it.getOrderTime(addPeriodBeforeOrder) <= start.toInstant(ZoneOffset.UTC)
                                && it.getSupposedFinishTime() <= start.toInstant(ZoneOffset.UTC)
                                || it.getOrderTime(addPeriodBeforeOrder) >= start.plusHours(BREAK_DURATION_HS).toInstant(ZoneOffset.UTC)
                                && it.getSupposedFinishTime() >= start.plusHours(BREAK_DURATION_HS).toInstant(ZoneOffset.UTC))

                    }

            breakTimeList.add(
                BreakTimeDistribution(
                    timeStart = start,
                    timeFinish = start.plusHours(BREAK_DURATION_HS),
                    orders = ordersBetweenTime,
                    employeesNeedWork = ordersBetweenTime.sumOf { it.maleEmployeeCount + it.femaleEmployeeCount },
                    employeesFree =
                    employeeTimePlanList
                        .filter { employeeTimeFreeOn(start, start.plusHours(BREAK_DURATION_HS), it.timePlan) }
                        .count()
                )
            )

            start = start.plusHours(BREAK_DURATION_HS)
        }

        // распределяем обеды на время без заявок
        breakTimeList
            .sortedBy { it.timeStart }
            .filter { it.orders.count() == 0 }
            .forEach { breakTimePriority ->
                employeeTimePlanList
                    .filter {
                        !planHasBreak(it.timePlan)
                    }
                    .filter {
                        employeeCanTakeBreakOn(
                            employee = it.employee,
                            planDate = planDate,
                            startTime = breakTimePriority.timeStart,
                            finishTime = breakTimePriority.timeFinish
                        )
                    }
                    .filter {
                        employeeTimeFreeOn(
                            startTime = breakTimePriority.timeStart,
                            finishTime = breakTimePriority.timeFinish,
                            plans = it.timePlan
                        )
                    }
                    .let {
                        it.forEach {
                            it.timePlan.add(
                                EmployeeShiftOrder(
                                    timeStart = breakTimePriority.timeStart,
                                    timeFinish = breakTimePriority.timeFinish,
                                    actionType = TimeListActionType.BREAK,
                                    order = null
                                )
                            )
                        }
                    }
            }


        if (allEmployeesHasBreak(employeeTimePlanList)) {
            return
        }


        var breakPriority = 0
        while (!allEmployeesHasBreak(employeeTimePlanList)) {
            breakTimeList
                .filter { it.orders.isNotEmpty() }
                .sortedBy { it.timeStart }
                .forEach { breakTimePriority ->
                    val employeeForBreakCount = breakTimePriority.employeesFree - breakTimePriority.employeesNeedWork
                    if (employeeForBreakCount + breakPriority > 0) {

                        for (i in 1..(employeeForBreakCount + breakPriority)) {

                            val validEmployeesForBreak =
                                employeeTimePlanList
                                .filter { !planHasBreak(it.timePlan) }
                                .filter {
                                    employeeCanTakeBreakOn(
                                        employee = it.employee,
                                        planDate = planDate,
                                        startTime = breakTimePriority.timeStart,
                                        finishTime = breakTimePriority.timeFinish
                                    )
                                }
                                .filter {
                                    employeeTimeFreeOn(
                                        startTime = breakTimePriority.timeStart,
                                        finishTime = breakTimePriority.timeFinish,
                                        plans = it.timePlan
                                    )
                                }

                            if (validEmployeesForBreak.isNotEmpty()) {

                                validEmployeesForBreak
                                    .take(if (employeeForBreakCount + breakPriority > validEmployeesForBreak.size) validEmployeesForBreak.size else employeeForBreakCount + breakPriority)
                                    .let {
                                        it.forEach {
                                            it.timePlan.add(
                                                EmployeeShiftOrder(
                                                    timeStart = breakTimePriority.timeStart,
                                                    timeFinish = breakTimePriority.timeFinish,
                                                    actionType = TimeListActionType.BREAK,
                                                    order = null
                                                )
                                            )
                                        }
                                    }

                                breakTimePriority.orders
                                    .filter { !orderAlreadyDisturb(employeeTimePlanList, it) }
                                    .forEach { order ->
                                        employeeTimePlanList
                                            .filter {
                                                !planHasOrder(it.timePlan, order)
                                                        &&
                                                        employeeTimeFreeOn(
                                                            startTime = LocalDateTime.ofInstant(
                                                                order.getOrderTime(addPeriodBeforeOrder),
                                                                TIME_ZONE_UTC
                                                            ),
                                                            finishTime = LocalDateTime.ofInstant(
                                                                order.getSupposedFinishTime(),
                                                                TIME_ZONE_UTC
                                                            ),
                                                            plans = it.timePlan
                                                        )
                                            }
                                            .forEach {
                                                it.timePlan.add(
                                                    EmployeeShiftOrder(
                                                        timeStart = LocalDateTime.ofInstant(
                                                            order.getOrderTime(addPeriodBeforeOrder),
                                                            TIME_ZONE_UTC
                                                        ),
                                                        timeFinish = LocalDateTime.ofInstant(
                                                            order.getSupposedFinishTime(),
                                                            TIME_ZONE_UTC
                                                        ),
                                                        actionType = TimeListActionType.GUESSING_TECHNICAL_TYPE,
                                                        order = order
                                                    )
                                                )
                                            }
                                    }

                            }
                        }

                    }
                }

            breakPriority++
        }


        employeeTimePlanList.forEach { it.timePlan.removeAll { it.actionType == TimeListActionType.GUESSING_TECHNICAL_TYPE } }

    }

    private fun allEmployeesHasBreak(
        employeeTimePlanList: List<OrderTime>
    ): Boolean {
        return employeeTimePlanList
            .all { planHasBreak(it.timePlan) }
    }

    private fun planHasBreak(
        plans: List<EmployeeShiftOrder>
    ): Boolean {
        return plans.any { it.actionType == TimeListActionType.BREAK }
    }

    private fun planHasOrder(
        plans: List<EmployeeShiftOrder>,
        order: PassengerOrder,
    ): Boolean {
        return plans.any { it.order == order }
    }

    private fun orderAlreadyDisturb(
        plans: List<OrderTime>,
        order: PassengerOrder,
    ): Boolean {
        return plans.any { planHasOrder(it.timePlan, order) }
    }

    private fun employeeWorkWhenMetroStop(
        employee: Employee
    ): Boolean {
        return employee.workStart > employee.workFinish
    }

    private fun employeeTimeFreeOn(
        startTime: LocalDateTime,
        finishTime: LocalDateTime,
        plans: List<EmployeeShiftOrder>
    ): Boolean {
        return plans
            .all {
                it.timeStart <= startTime && it.timeFinish <= startTime
                        || it.timeStart >= finishTime && it.timeFinish >= finishTime
            }
    }

    private fun employeeCanTakeBreakOn(
        employee: Employee,
        planDate: LocalDate,
        startTime: LocalDateTime,
        finishTime: LocalDateTime,
    ): Boolean {
        return LocalDateTime.of(planDate, employee.workStart).plusMinutes(180) <= startTime
                && LocalDateTime.of(planDate, employee.workFinish).minusMinutes(60) >= finishTime
    }

    fun hackCleanAllNightBreakTime(
        employeeTimePlanList: List<OrderTime>
    ) {
        employeeTimePlanList
            .forEach { it.timePlan.removeAll {
                it.actionType == TimeListActionType.BREAK
                        && it.timeStart.toLocalTime() == LocalTime.of(3, 0)
                        && it.timeFinish.toLocalTime() == LocalTime.of(4, 0)
            } }
    }

    companion object {
        private val BREAK_DURATION_HS = 1L
        private val TIME_ZONE_UTC = ZoneId.of("UTC")
    }
}
