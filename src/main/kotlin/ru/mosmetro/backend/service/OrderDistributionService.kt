package ru.mosmetro.backend.service

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.mosmetro.backend.mapper.EmployeeMapper
import ru.mosmetro.backend.mapper.EmployeeShiftOrderMapper
import ru.mosmetro.backend.mapper.OrderMapper
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.domain.EmployeePriority
import ru.mosmetro.backend.model.domain.EmployeeShiftOrder
import ru.mosmetro.backend.model.domain.MetroStation
import ru.mosmetro.backend.model.domain.OrderBaggage
import ru.mosmetro.backend.model.domain.OrderTime
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.order.OrderTimeDTO
import ru.mosmetro.backend.model.dto.order.OrderTimeListDTO
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.model.enums.PassengerCategoryType
import ru.mosmetro.backend.model.enums.SexType
import ru.mosmetro.backend.model.enums.TimeListActionType
import ru.mosmetro.backend.util.MetroTimeUtil.METRO_TIME_FINISH
import ru.mosmetro.backend.util.MetroTimeUtil.METRO_TIME_START
import ru.mosmetro.backend.util.MetroTimeUtil.MIN_TRANSFER_TIME_PERIOD_SEC
import ru.mosmetro.backend.util.MetroTimeUtil.TIME_ZONE_UTC
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
class OrderDistributionService(
    private val timeListService: TimeListService,
    private val orderService: OrderService,
    private val metroTransfersService: MetroTransfersService,
    private val breakTimeGuesserService: BreakTimeGuesserService,

    private val employeeMapper: EmployeeMapper,
    private val orderMapper: OrderMapper,
    private val employeeShiftOrderMapper: EmployeeShiftOrderMapper
) {

    suspend fun calculateOrderDistribution(
        planDate: LocalDate
    ): ListWithTotal<OrderTimeDTO> {
        val result: OrderTimeListDTO = calculateOrderDistribution(planDate, true, true, true)

        return ListWithTotal(result.ordersTime.size, result.ordersTime)
    }

    fun calculateOrderDistributionForTest(
        planDate: LocalDate
    ): OrderTimeListDTO {
        return runBlocking { calculateOrderDistribution(planDate, false, false, false) }
    }

    suspend fun calculateOrderDistribution(
        planDate: LocalDate,
        guessBreakTime: Boolean,
        addTransferPeriod: Boolean,
        addPeriodBeforeOrder: Boolean,
    ): OrderTimeListDTO {
        // получаем всех занятых сотрудников исходя из их графика работы
        // создаем на всех лист занятости
        val employeeTimePlanList: List<OrderTime> = timeListService.getOrderTimeList(planDate)

        // получаем все активные заявки в дату составления плана
        // сортируем заявки по времени
        val orderStartTime = LocalDateTime.of(planDate, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(planDate, METRO_TIME_FINISH)
        val passengerOrderList =
            orderService.getOrdersBetweenOrderDate(orderStartTime, orderFinishTime)
                .filter { it.orderStatus.code != OrderStatusType.CANCELED && it.orderStatus.code != OrderStatusType.REJECTED }
                .sortedWith(compareBy({ it.getOrderTime(addPeriodBeforeOrder) }, { it.createdAt }))

        if (guessBreakTime) {
            breakTimeGuesserService.guessBreakTime(planDate, employeeTimePlanList, passengerOrderList, addPeriodBeforeOrder)

            // TODO график фронта не может отобразить время за пределами 01-00
            //      считаем что в ночное время обеды распределять не нужно
            breakTimeGuesserService.hackCleanAllNightBreakTime(employeeTimePlanList)
        }

        val orderNotInPlanList: MutableList<PassengerOrder> = mutableListOf()

        for (passengerOrder in passengerOrderList) {
            // получаем всех свободных сотрудников на время заявки
            val notBusyEmployeeList: List<OrderTime> = getTimeFreeEmployeeList(
                planDate,
                passengerOrder,
                employeeTimePlanList,
                LocalDateTime.ofInstant(passengerOrder.getOrderTime(addPeriodBeforeOrder), TIME_ZONE_UTC),
                LocalDateTime.ofInstant(passengerOrder.getSupposedFinishTime(), TIME_ZONE_UTC),
                addTransferPeriod,
                addPeriodBeforeOrder
            )

            if (notBusyEmployeeList.isEmpty()
                || !hasNeededEmployeeCount(notBusyEmployeeList.map { it.employee }, passengerOrder)
            ) {
                orderNotInPlanList.add(passengerOrder)
                continue
            }

            // считаем приоритет на каждого сотрудника по текущей заявке
            val employeePriorityList: List<EmployeePriority> =
                makeEmployeePriorityList(notBusyEmployeeList, passengerOrder, addTransferPeriod, addPeriodBeforeOrder)

            val employeeForOrderList: MutableList<EmployeePriority> = mutableListOf()
            // начинаем распределять с женщин
            var femaleEmployeeOrderCount = passengerOrder.femaleEmployeeCount
            while (
                femaleEmployeeOrderCount > 0
                && hasSuitableEmployee(
                    employeePriorityList,
                    passengerOrder.passengerCategory,
                    passengerOrder.baggage,
                    SexType.FEMALE
                )
            ) {
                val priorityEmployee =
                    getMostPriorityEmployee(
                        employeeForOrderList,
                        employeePriorityList,
                        passengerOrder.passengerCategory,
                        passengerOrder.baggage,
                        SexType.FEMALE
                    )
                if (priorityEmployee == null) {
                    orderNotInPlanList.add(passengerOrder)
                    femaleEmployeeOrderCount = 0
                } else {
                    employeeForOrderList.add(priorityEmployee)
                    femaleEmployeeOrderCount--
                }
            }

            // распределям мужчин
            // к мужчинам добавляем нераспределенных женщин
            var maleEmployeeOrderCount = passengerOrder.maleEmployeeCount + femaleEmployeeOrderCount
            while (maleEmployeeOrderCount > 0 && hasSuitableEmployee(
                    employeePriorityList,
                    passengerOrder.passengerCategory,
                    passengerOrder.baggage,
                    SexType.MALE
                )
            ) {
                val priorityEmployee =
                    getMostPriorityEmployee(
                        employeeForOrderList,
                        employeePriorityList,
                        passengerOrder.passengerCategory,
                        passengerOrder.baggage,
                        SexType.MALE
                    )
                if (priorityEmployee == null) {
                    orderNotInPlanList.add(passengerOrder)
                    maleEmployeeOrderCount = 0
                } else {
                    employeeForOrderList.add(priorityEmployee)
                    maleEmployeeOrderCount--
                }
            }

            employeeForOrderList.forEach {
                addBusyTimeToTimePlan(employeeTimePlanList, it.employee, it.transferTime, passengerOrder, addPeriodBeforeOrder)
            }
        }

        // обогощаем всем временем
        val allTimeEmployeeTimePlanList = timeListService.addAllTime(
            planDate,
            employeeTimePlanList
        )

        val result: List<OrderTime> =
            allTimeEmployeeTimePlanList
                .map {
                    val truncateSecondsPlan = it.timePlan.map {
                        EmployeeShiftOrder(
                            timeStart = it.timeStart.truncatedTo(ChronoUnit.MINUTES),
                            timeFinish = it.timeFinish.truncatedTo(ChronoUnit.MINUTES),
                            actionType = it.actionType,
                            order = it.order,
                        )
                    }.toMutableList()

                    return@map OrderTime(it.employee, truncateSecondsPlan)
                }.toList()

        val orderTimeList = result
            .map {
                OrderTimeDTO(
                    employee = employeeMapper.domainToDto(it.employee),
                    actions = it.timePlan.map { employeeShiftOrderMapper.domainToDto(it) }.sortedBy { it.timeStart }
                )
            }

        return OrderTimeListDTO(
            ordersNotInPlan = orderNotInPlanList.map { orderMapper.domainToDto(it) },
            ordersTime = orderTimeList
        )
    }

    private fun addBusyTimeToTimePlan(
        employeeTimePlanList: List<OrderTime>,
        employee: Employee,
        transferTime: Duration,
        order: PassengerOrder,
        addPeriodBeforeOrder: Boolean,
    ) {
        employeeTimePlanList
            .find { it.employee == employee }
            .let {
                if (!transferTime.isZero) {
                    it!!.timePlan.add(
                        EmployeeShiftOrder(
                            timeStart = LocalDateTime.ofInstant(order.getOrderTime(addPeriodBeforeOrder), TIME_ZONE_UTC)
                                .minusSeconds(transferTime.toSeconds()),
                            timeFinish = LocalDateTime.ofInstant(order.getOrderTime(addPeriodBeforeOrder), TIME_ZONE_UTC),
                            actionType = TimeListActionType.TRANSFER,
                            order = null
                        )
                    )
                }

                it!!.timePlan.add(
                    EmployeeShiftOrder(
                        timeStart = LocalDateTime.ofInstant(order.getOrderTime(addPeriodBeforeOrder), TIME_ZONE_UTC),
                        timeFinish = LocalDateTime.ofInstant(order.getSupposedFinishTime(), TIME_ZONE_UTC),
                        actionType = TimeListActionType.ORDER,
                        order = order
                    )
                )
            }
    }

    private fun hasSuitableEmployee(
        priorityEmployeeList: List<EmployeePriority>,
        passengerCategory: PassengerCategoryType, // TODO
        baggage: OrderBaggage?,
        sexType: SexType
    ): Boolean {
        return priorityEmployeeList
            .any {
                if (baggage != null && baggage.isHelpNeeded) {
                    it.employee.sex == sexType && !it.employee.lightDuties
                } else {
                    it.employee.sex == sexType
                }
            }
    }

    private fun hasNeededEmployeeCount(
        employeeList: List<Employee>,
        order: PassengerOrder
    ): Boolean {
        val employeeNeedsCount = order.maleEmployeeCount + order.femaleEmployeeCount
        val actualCount = employeeList
            .filter {
                var flag = true

                if (order.baggage != null && order.baggage.isHelpNeeded) {
                    flag = flag && !it.lightDuties
                }

                return@filter flag
            }.count()

        return employeeNeedsCount <= actualCount
    }

    private fun getMostPriorityEmployee(
        employeeAlreadyChooseList: List<EmployeePriority>,
        priorityEmployeeList: List<EmployeePriority>,
        passengerCategory: PassengerCategoryType, // TODO
        baggage: OrderBaggage?,
        sexType: SexType
    ): EmployeePriority? {
        return priorityEmployeeList
            .filter { empl -> employeeAlreadyChooseList.all { it.employee != empl.employee } }
            .filter {
                if (baggage != null && baggage.isHelpNeeded) {
                    it.employee.sex == sexType && !it.employee.lightDuties
                } else {
                    it.employee.sex == sexType
                }
            }
            .minByOrNull { it.transferTime }
    }

    private fun getTimeFreeEmployeeList(
        planDate: LocalDate,
        order: PassengerOrder,
        timeLineEmployee: List<OrderTime>,
        orderStartTime: LocalDateTime,
        orderFinishTime: LocalDateTime,
        addTransferPeriod: Boolean,
        addPeriodBeforeOrder: Boolean,
    ): List<OrderTime> {
        return timeLineEmployee
            .filter {
                // смена сотрудника позволяет взять заявку
                if (it.employee.workStart > it.employee.workFinish)
                    LocalDateTime.of(planDate, it.employee.workStart) <= orderStartTime
                            && LocalDateTime.of(planDate.plusDays(1), it.employee.workFinish) >= orderFinishTime
                else
                    LocalDateTime.of(planDate, it.employee.workStart) <= orderStartTime
                            && LocalDateTime.of(planDate, it.employee.workFinish) >= orderFinishTime
            }
            .filter {
                // сотрудник свободен по графику дня
                it.timePlan.all { plan -> plan.timeFinish <= orderStartTime || plan.timeStart >= orderFinishTime }
            }
            .filter {   // сотрудник успеет приехать на станцию
                // если пока не занят
                if (it.timePlan.isEmpty()) {
                    return@filter true

                // если занят
                } else {
                    val planBefore = it.timePlan
                        .filter { it.timeFinish.toInstant(ZoneOffset.UTC) <= order.getOrderTime(addPeriodBeforeOrder) }
                        .filter { it.order != null }
                        .sortedBy { it.timeStart }
                    // по закрепленному плану свободен
                    if (planBefore.isEmpty()) {
                        return@filter true
                    } else {
                        val timeTransferSeconds =
                            addTransferPeriodTime(
                                metroTransfersService.calculateMetroStationTransfersDuration(
                                    planBefore.last().order!!.finishStation,
                                    order.startStation
                                ),
                                addTransferPeriod
                            )

                        planBefore.last().timeFinish.plusSeconds(timeTransferSeconds).toInstant(ZoneOffset.UTC) <= order.getOrderTime(addPeriodBeforeOrder)
                    }
                }
            }
            .toList()
    }

    private fun makeEmployeePriorityList(
        orderTimes: List<OrderTime>,
        order: PassengerOrder,
        addTransferPeriod: Boolean,
        addPeriodBeforeOrder: Boolean,
    ): List<EmployeePriority> {
        return orderTimes
            .map {
                var priority = 0

                val timeTransfer = calculateMetroTransferTime(it, order, addTransferPeriod, addPeriodBeforeOrder)
                priority += calculateMetroTransferPriority(timeTransfer) // приоритет по времени в пути сотрудника на заявку

                EmployeePriority(it.employee, timeTransfer, priority)
            }
            .toList()
    }

    private fun calculateMetroTransferTime(
        orderTime: OrderTime,
        order: PassengerOrder,
        addTransferPeriod: Boolean,
        addPeriodBeforeOrder: Boolean,
    ): Duration {
        val orderStartStation = order.startStation
        val employeeStation = getEmployeeStation(orderTime, order, addPeriodBeforeOrder)
        return if (employeeStation == null) Duration.ZERO else Duration.ofSeconds(
            addTransferPeriodTime(
                metroTransfersService.calculateMetroStationTransfersDuration(
                    employeeStation,
                    orderStartStation
                ),
                addTransferPeriod
            )
        )
    }

    private fun addTransferPeriodTime(
        transferDurationSec: Long,
        addTransferPeriod: Boolean
    ): Long {
        return if (addTransferPeriod) transferDurationSec + MIN_TRANSFER_TIME_PERIOD_SEC else transferDurationSec
    }

    private fun calculateMetroTransferPriority(
        timeTransfer: Duration,
    ): Int {
        if (timeTransfer.toMinutes() < 15) {
            return 4
        } else if (timeTransfer.toMinutes() < 30) {
            return 2
        } else if (timeTransfer.toMinutes() < 60) {
            return 1
        }

        return 0
    }

    private fun getEmployeeStation(
        orderTime: OrderTime,
        order: PassengerOrder,
        addPeriodBeforeOrder: Boolean,
    ): MetroStation? {
        // по плану свободен
        if (orderTime.timePlan.isEmpty()) {
            return null
        }

        val planBefore = orderTime.timePlan
            .filter { it.timeFinish.toInstant(ZoneOffset.UTC) <= order.getOrderTime(addPeriodBeforeOrder) }
            .filter { it.order != null }
            .sortedBy { it.timeStart }

        return if (planBefore.isEmpty()) null else planBefore.last().order!!.finishStation
    }

}
