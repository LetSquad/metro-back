package ru.mosmetro.backend.service

import org.junit.jupiter.api.Assertions
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.domain.EmployeeRank
import ru.mosmetro.backend.model.domain.EmployeeShiftOrder
import ru.mosmetro.backend.model.domain.MetroStation
import ru.mosmetro.backend.model.domain.OrderApplication
import ru.mosmetro.backend.model.domain.OrderBaggage
import ru.mosmetro.backend.model.domain.OrderStatus
import ru.mosmetro.backend.model.domain.OrderTime
import ru.mosmetro.backend.model.domain.Passenger
import ru.mosmetro.backend.model.domain.PassengerCategory
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.dto.employee.EmployeeDTO
import ru.mosmetro.backend.model.dto.order.EmployeeShiftOrderDTO
import ru.mosmetro.backend.model.dto.order.OrderTimeDTO
import ru.mosmetro.backend.model.enums.EmployeeRoleType
import ru.mosmetro.backend.model.enums.OrderApplicationType
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.model.enums.SexType
import ru.mosmetro.backend.model.enums.TimeListActionType
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

object ServiceTestUtil {

    fun makeOrderTime(
        employee: Employee,
        plan: List<EmployeeShiftOrder>
    ): OrderTime {
        return OrderTime(
            employee = employee,
            timePlan = plan.toMutableList()
        )
    }

    fun makeEmployee(
        id: Long,
        sex: SexType,
        workStart: LocalTime,
        workFinish: LocalTime,
        lightDuties: Boolean
    ): Employee {
        return Employee(
            id = id,
            firstName = "",
            lastName = "",
            middleName = "",
            sex = sex,
            // TimeZone MOS = -3h
            workStart = workStart.minusHours(3),
            workFinish = workFinish.minusHours(3),
            shiftType = "",
            workPhone = "",
            personalPhone = "",
            employeeNumber = 0L,
            lightDuties = lightDuties,
            rank = EmployeeRank(
                code = "",
                name = "",
                shortName = "",
                role = EmployeeRoleType.ADMIN.name,
                ),
            login = "",
            isPasswordTemp = false
        )
    }

    fun makePassengerOrder(
        id: Long,
        maleEmployeeCount: Int,
        femaleEmployeeCount: Int,
        orderTime: LocalDateTime,
        finishTime: LocalDateTime,
        orderStatusType: OrderStatusType,
        baggage: OrderBaggage?,
        passengerCategory: PassengerCategory,
        startMetroStation: MetroStation,
        finishMetroStation: MetroStation,
    ): PassengerOrder {
        return PassengerOrder(
            id = id,
            startDescription = null,
            finishDescription = null,
            orderApplication = OrderApplication(OrderApplicationType.ELECTRONIC_SERVICES, "Электронные сервисы"),
            passengerCount = maleEmployeeCount + femaleEmployeeCount,
            maleEmployeeCount = maleEmployeeCount,
            femaleEmployeeCount = femaleEmployeeCount,
            duration = Duration.between(orderTime, finishTime),
            additionalInfo = null,
            // TimeZone MOS = -3h
            orderTime = orderTime.minusHours(3).toInstant(ZoneOffset.UTC),
            startTime = Instant.now(),
            finishTime = null,
            absenceTime = null,
            cancelTime = null,
            createdAt = Instant.now(),
            updatedAt = null,
            deletedAt = null,
            orderStatus = OrderStatus(orderStatusType, ""),
            passenger = Passenger(
                id = null,
                firstName = "",
                lastName = "",
                middleName = "",
                sex = SexType.MALE,
                comment = null,
                hasPacemaker = null,
                createdAt = Instant.now(),
                deletedAt = null,
                category = passengerCategory,
                phones = emptySet()
            ),
            baggage = baggage,
            transfers = listOf(),
            passengerCategory = passengerCategory.code,
            startStation = startMetroStation,
            finishStation = finishMetroStation,
            employees = null
        )
    }

    fun makePassengerOrder(
        id: Long,
        maleEmployeeCount: Int,
        femaleEmployeeCount: Int,
        createdAt: LocalDateTime,
        orderTime: LocalDateTime,
        finishTime: LocalDateTime,
        orderStatusType: OrderStatusType,
        baggage: OrderBaggage?,
        passengerCategory: PassengerCategory,
        startMetroStation: MetroStation,
        finishMetroStation: MetroStation,
    ): PassengerOrder {
        return PassengerOrder(
            id = id,
            startDescription = null,
            finishDescription = null,
            orderApplication = OrderApplication(OrderApplicationType.ELECTRONIC_SERVICES, "Электронные сервисы"),
            passengerCount = maleEmployeeCount + femaleEmployeeCount,
            maleEmployeeCount = maleEmployeeCount,
            femaleEmployeeCount = femaleEmployeeCount,
            duration = Duration.between(orderTime, finishTime),
            additionalInfo = null,
            // TimeZone MOS = -3h
            orderTime = orderTime.minusHours(3).toInstant(ZoneOffset.UTC),
            startTime = Instant.now(),
            finishTime = null,
            absenceTime = null,
            cancelTime = null,
            createdAt = createdAt.toInstant(ZoneOffset.UTC),
            updatedAt = null,
            deletedAt = null,
            orderStatus = OrderStatus(orderStatusType, ""),
            passenger = Passenger(
                id = null,
                firstName = "",
                lastName = "",
                middleName = "",
                sex = SexType.MALE,
                comment = null,
                hasPacemaker = null,
                createdAt = Instant.now(),
                deletedAt = null,
                category = passengerCategory,
                phones = emptySet()
            ),
            baggage = baggage,
            transfers = listOf(),
            passengerCategory = passengerCategory.code,
            startStation = startMetroStation,
            finishStation = finishMetroStation,
            employees = null
        )
    }

    fun makeEmployeeShiftOrder(
        timeStart: LocalDateTime,
        timeFinish: LocalDateTime,
        actionType: TimeListActionType,
        order: PassengerOrder?,
    ): EmployeeShiftOrder {
        return EmployeeShiftOrder(
            // TimeZone MOS = -3h
            timeStart = timeStart.minusHours(3),
            timeFinish = timeFinish.minusHours(3),
            actionType = actionType,
            order = order
        )
    }

    fun makeOrderBaggage(
        isHelpNeeded: Boolean
    ): OrderBaggage {
        return OrderBaggage(
            type = "",
            weight = 0,
            isHelpNeeded = isHelpNeeded,
        )
    }


    fun makeMetroStation(
        name: String,
    ): MetroStation {
        return MetroStation(
            id = null,
            name = name,
            line = null,
        )
    }

    fun assertOrderTimeDTOList(
        actual: List<OrderTimeDTO>,
        expected: List<OrderTimeDTO>
    ) {
        Assertions.assertEquals(expected.size, actual.size)

        expected
            .forEachIndexed { index, value ->
                assertOrderTimeDTO(actual[index], value)
            }
    }

    fun assertOrderTimeList(
        actual: List<OrderTime>,
        expected: List<OrderTime>
    ) {
        Assertions.assertEquals(expected.size, actual.size)

        expected
            .forEachIndexed { index, value ->
                assertOrderTime(actual[index], value)
            }
    }

    fun getActionListStr(
        employee: EmployeeDTO,
        actions: List<EmployeeShiftOrderDTO>
    ): String {
        return actions
            .sortedBy { it.timeStart }
            .map {
                val strBuilder = StringBuilder()
                strBuilder.append("\n")
                strBuilder.append("\nemployee: ").append(employee.id)
                strBuilder.append("\nstart: ").append(LocalDateTime.ofInstant(it.timeStart, ZoneId.of("+03:00")))
                strBuilder.append("\nfinish: ").append(LocalDateTime.ofInstant(it.timeEnd, ZoneId.of("+03:00")))
                strBuilder.append("\ntype: ").append(it.actionType)
                strBuilder.append("\norder: ").append(it.order)

                strBuilder.toString()
            }.toString()
    }

    fun getActionListStr(
        employee: Employee,
        actions: List<EmployeeShiftOrder>
    ): String {
        return actions
            .sortedBy { it.timeStart }
            .map {
                val strBuilder = StringBuilder()
                strBuilder.append("\n")
                strBuilder.append("\nemployee: ").append(employee.id)
                strBuilder.append("\nstart: ").append(it.timeStart)
                strBuilder.append("\nfinish: ").append(it.timeFinish)
                strBuilder.append("\ntype: ").append(it.actionType)
                strBuilder.append("\norder: ").append(it.order)

                strBuilder.toString()
            }.toString()
    }

    fun assertOrderTimeDTO(
        actual: OrderTimeDTO,
        expected: OrderTimeDTO
    ) {
        Assertions.assertEquals(expected.employee, actual.employee)

        Assertions.assertEquals(expected.actions.size, actual.actions.size, getActionListStr(actual.employee, actual.actions))

        val expectedActions = expected.actions.sortedBy { it.timeStart }
        val actualActions = actual.actions.sortedBy { it.timeStart }

        expectedActions
            .sortedBy { it.timeStart }
            .forEachIndexed { index, value ->
                Assertions.assertEquals(value.timeStart, actualActions[index].timeStart)
                Assertions.assertEquals(value.timeEnd, actualActions[index].timeEnd)
                Assertions.assertEquals(value.actionType, actualActions[index].actionType)
                Assertions.assertEquals(value.order, actualActions[index].order)
            }
    }

    fun assertOrderTime(
        actual: OrderTime,
        expected: OrderTime
    ) {
        Assertions.assertEquals(expected.employee, actual.employee)

        Assertions.assertEquals(expected.timePlan.size, actual.timePlan.size, getActionListStr(actual.employee, actual.timePlan))

        val expectedActions = expected.timePlan.sortedBy { it.timeStart }
        val actualActions = actual.timePlan.sortedBy { it.timeStart }

        expectedActions
            .sortedBy { it.timeStart }
            .forEachIndexed { index, value ->
                Assertions.assertEquals(value.timeStart, actualActions[index].timeStart)
                Assertions.assertEquals(value.timeFinish, actualActions[index].timeFinish)
                Assertions.assertEquals(value.actionType, actualActions[index].actionType)
                Assertions.assertEquals(value.order, actualActions[index].order)
            }
    }

}