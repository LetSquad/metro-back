package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.domain.MetroStation
import ru.mosmetro.backend.model.domain.Order
import ru.mosmetro.backend.model.domain.combi.EmployeePriority
import ru.mosmetro.backend.model.domain.combi.EmployeeTimePlan
import ru.mosmetro.backend.model.domain.combi.TimePlan
import ru.mosmetro.backend.model.enums.SexType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class OrdersDistributionService(
    val metroTransfersService: MetroTransfersService
) {

    // нет обеда
    // нет выдачи нераспределенных заявок
    fun autoDistribution(
        allEmployees: List<Employee>,
        allOrders: List<Order>,
        planDate: LocalDate
    ): Map<Order, List<Employee>> {
        val result: MutableMap<Order, MutableList<Employee>> = mutableMapOf()

        // получаем всех занятых сотрудников исходя из их графика работы
        val workingEmployeeList = getAllWorkingEmployeesByDate(allEmployees, planDate)
        // создаем на всех лист занятости
        val timeLineEmployee: List<EmployeeTimePlan> = makeEmployeeTimePlan(workingEmployeeList)

        // получаем все активные заявки в дату составления плана
        // сортируем заявки по времени
        val workingOrderList = getAllWorkingOrdersByDate(allOrders, planDate).sortedBy { it.startTime }

        for (orderForDistribution in workingOrderList) {
            // получаем всех свободных сотрудников на время заявки
            val notBusyEmployeeList: List<EmployeeTimePlan> = getNotBusyEmployeeList(
                orderForDistribution,
                timeLineEmployee,
                orderForDistribution.startTime,
                orderForDistribution.finishTime
            )
            // считаем приоритет на каждого сотрудника по текущей заявке
            val employeePriorityList: List<EmployeePriority> =
                makeEmployeePriorityList(notBusyEmployeeList, orderForDistribution)

            var femaleEmployeeOrderCount = orderForDistribution.femaleEmployeeCount
            while (femaleEmployeeOrderCount > 0 && hasEmployeeBySex(employeePriorityList, SexType.FEMALE)) {
                val priorityEmployee = getMostPriorityEmployee(employeePriorityList, SexType.FEMALE)
                val employeeTimePlan = notBusyEmployeeList.find { it.employee.id == priorityEmployee.employee.id }!!

                addBusyTimeOnEmployeeTimePlan(priorityEmployee.transferTime, employeeTimePlan, orderForDistribution)

                result.getOrPut(orderForDistribution) { mutableListOf() }.add(priorityEmployee.employee)
                femaleEmployeeOrderCount--
            }

            var maleEmployeeOrderCount = orderForDistribution.maleEmployeeCount + femaleEmployeeOrderCount
            while (maleEmployeeOrderCount > 0 && hasEmployeeBySex(employeePriorityList, SexType.MALE)) {
                val priorityEmployee = getMostPriorityEmployee(employeePriorityList, SexType.MALE)
                val employeeTimePlan = notBusyEmployeeList.find { it.employee.id == priorityEmployee.employee.id }!!

                addBusyTimeOnEmployeeTimePlan(priorityEmployee.transferTime, employeeTimePlan, orderForDistribution)

                result.getOrPut(orderForDistribution) { mutableListOf() }.add(priorityEmployee.employee)
                maleEmployeeOrderCount--
            }
        }

        return result
    }

    private fun addBusyTimeOnEmployeeTimePlan(
        transferTime: Duration,
        employeeTimePlan: EmployeeTimePlan,
        order: Order
    ) {
        if (!transferTime.isZero) {
            employeeTimePlan.timePlan.add(TimePlan(order.startTime.minusMinutes(transferTime.toMinutes()), order.startTime, order, true))
        }

        employeeTimePlan.timePlan.add(TimePlan(order.startTime, order.finishTime, order, false))
    }

    private fun getMostPriorityEmployee(
        priorityEmployeeList: List<EmployeePriority>,
        sexType: SexType
    ): EmployeePriority {
        return priorityEmployeeList
            .filter { it.employee.sex == sexType }.maxByOrNull { it.priority }!!
    }

    private fun hasEmployeeBySex(
        employeeList: List<EmployeePriority>,
        sexType: SexType
    ): Boolean {
        return employeeList.any { it.employee.sex == sexType }
    }

    private fun getNotBusyEmployeeList(
        order: Order,
        timeLineEmployee: List<EmployeeTimePlan>,
        startTime: LocalDateTime,
        finishTime: LocalDateTime
    ): List<EmployeeTimePlan> {
        return timeLineEmployee
            .filter {
                it.timePlan.all { timeLine -> timeLine.finishTime < startTime || timeLine.startTime > finishTime }  // сотрудник свободен по графику дня
                        && it.employee.workStart <= startTime.toLocalTime() && it.employee.workFinish >= finishTime.toLocalTime() // смена сотрудника позволяет взять заявку
            }
            .filter {   // сотрудник успеет приехать на станцию
                if (it.timePlan.isEmpty()) {
                    true
                } else {
                    val timeTransferSeconds = metroTransfersService.calculateTransferDuration(
                        it.timePlan.last().order.finishStation,
                        order.startStation
                    )

                    it.timePlan.last().finishTime.plusSeconds(timeTransferSeconds) <= order.startTime
                }
            }
            .toList()
    }

    private fun makeEmployeePriorityList(
        allTimeFreeEmployee: List<EmployeeTimePlan>,
        order: Order
    ): List<EmployeePriority> {
        return allTimeFreeEmployee
            .map {
                var priority = 0

                val timeTransfer = calculateMetroTransferTime(it, order)
                priority += calculateMetroTransferPriority(timeTransfer) // приоритет по времени в пути сотрудника на заявку

                EmployeePriority(it.employee, timeTransfer, priority)
            }
            .toList()
    }

    private fun calculateMetroTransferTime(
        employeeTimePlan: EmployeeTimePlan,
        order: Order,
    ): Duration {
        val orderStartStation = order.startStation
        val employeeStation = getEmployeeStation(employeeTimePlan)
        return if (employeeStation == null) Duration.ZERO else Duration.ofSeconds(
            metroTransfersService.calculateTransferDuration(
                employeeStation,
                orderStartStation
            )
        )
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

    private fun getEmployeeStation(employeeTimePlan: EmployeeTimePlan): MetroStation? {
        return if (employeeTimePlan.timePlan.size != 0) employeeTimePlan.timePlan.last().order.finishStation else null
    }

    private fun getAllWorkingOrdersByDate(orderList: List<Order>, data: LocalDate): List<Order> {
        return orderList.filter { it.startTime.toLocalDate() == data }.toList()
    }

    private fun makeEmployeeTimePlan(validEmployeeList: List<Employee>): List<EmployeeTimePlan> {
        return validEmployeeList.map { EmployeeTimePlan(it, mutableListOf()) }.toList()
    }

    // TODO добавить маппинг на график работы
    private fun getAllWorkingEmployeesByDate(employeeList: List<Employee>, data: LocalDate): List<Employee> {
        return employeeList
    }

}