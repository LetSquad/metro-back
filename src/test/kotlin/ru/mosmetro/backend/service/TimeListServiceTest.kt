package ru.mosmetro.backend.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import ru.mosmetro.backend.mapper.EmployeeMapper
import ru.mosmetro.backend.mapper.OrderMapper
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.domain.EmployeeShiftOrder
import ru.mosmetro.backend.model.domain.OrderTime
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.dto.order.EmployeeShiftOrderDTO
import ru.mosmetro.backend.model.dto.order.OrderTimeDTO
import ru.mosmetro.backend.model.dto.order.OrderTimeListDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.entity.EmployeeShiftEntity
import ru.mosmetro.backend.model.entity.EmployeeShiftOrderEntity
import ru.mosmetro.backend.model.entity.MetroUserEntity
import ru.mosmetro.backend.model.enums.SexType
import ru.mosmetro.backend.model.enums.TimeListActionType
import ru.mosmetro.backend.repository.EmployeeShiftEntityRepository
import ru.mosmetro.backend.repository.EmployeeShiftOrderEntityRepository
import ru.mosmetro.backend.service.ServiceTestUtil.makeOrderTime
import ru.mosmetro.backend.util.MetroTimeUtil.METRO_TIME_FINISH
import ru.mosmetro.backend.util.MetroTimeUtil.METRO_TIME_START
import ru.mosmetro.backend.util.MetroTimeUtil.TIME_ZONE_MOSCOW
import ru.mosmetro.backend.util.MetroTimeUtil.TIME_ZONE_UTC
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.assertTrue

@SpringBootTest
class TimeListServiceTest {

    @SpyBean
    lateinit var employeeShiftOrderEntityRepository: EmployeeShiftOrderEntityRepository

    @SpyBean
    lateinit var employeeShiftEntityRepository: EmployeeShiftEntityRepository

    @Autowired
    lateinit var service: TimeListService

    @Autowired
    lateinit var employeeMapper: EmployeeMapper

    @Autowired
    lateinit var orderMapper: OrderMapper

    @Test
    fun test_addAllTime() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = ServiceTestUtil.makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val employee2 = ServiceTestUtil.makeEmployee(
            id = 2,
            sex = SexType.FEMALE,
            workStart = LocalTime.of(18, 0),
            workFinish = LocalTime.of(22, 0),
            lightDuties = false,
        )

        val plan: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee = employee1,
                    plan = listOf(
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(12, 0),
                            timeFinish = LocalTime.of(15, 0),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(17, 0),
                            timeFinish = LocalTime.of(17, 30),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                    )
                ),
                makeOrderTime(
                    employee = employee2,
                    plan = listOf(
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(18, 30),
                            timeFinish = LocalTime.of(19, 0),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(19, 0),
                            timeFinish = LocalTime.of(19, 30),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                    )
                )
            )

        // act
        val actual: List<OrderTime> = service.addAllTime(date, plan)

        // assert
        val expected =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = METRO_TIME_START.plusHours(3),
                            timeFinish = LocalTime.of(9, 0),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(9, 0),
                            timeFinish = LocalTime.of(12, 0),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(12, 0),
                            timeFinish = LocalTime.of(15, 0),
                            actionType = TimeListActionType.BREAK,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(15, 0),
                            timeFinish = LocalTime.of(17, 0),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(17, 0),
                            timeFinish = LocalTime.of(17, 30),
                            actionType = TimeListActionType.BREAK,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(17, 30),
                            timeFinish = LocalTime.of(18, 0),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH).plusHours(3),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null
                        ),
                    )
                ),
                makeOrderTime(
                    employee2,
                    listOf(
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = METRO_TIME_START.plusHours(3),
                            timeFinish = LocalTime.of(18, 0),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(18, 0),
                            timeFinish = LocalTime.of(18, 30),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(18, 30),
                            timeFinish = LocalTime.of(19, 0),
                            actionType = TimeListActionType.BREAK,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(19, 0),
                            timeFinish = LocalTime.of(19, 30),
                            actionType = TimeListActionType.BREAK,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(19, 30),
                            timeFinish = LocalTime.of(22, 0),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(22, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH).plusHours(3),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null
                        ),
                    )
                ),
            )

        ServiceTestUtil.assertOrderTimeList(
            actual.sortedBy { it.employee.id },
            expected.sortedBy { it.employee.id })
    }

    @Test
    fun test_addAllTime_withAllMetroWorkDay() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = ServiceTestUtil.makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(20, 0),
            workFinish = LocalTime.of(8, 0),
            lightDuties = false,
        )

        val plan: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee = employee1,
                    plan = listOf(
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(21, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(22, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(23, 0)),
                            timeFinish = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 30)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                    )
                ),
            )

        // act
        val actual: List<OrderTime> = service.addAllTime(date, plan)

        // assert
        val expected =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = METRO_TIME_START.plusHours(3),
                            timeFinish = LocalTime.of(20, 0),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(20, 0),
                            timeFinish = LocalTime.of(21, 0),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(21, 0),
                            timeFinish = LocalTime.of(22, 0),
                            actionType = TimeListActionType.BREAK,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(22, 0),
                            timeFinish = LocalTime.of(23, 0),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(23, 0)),
                            timeFinish = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 30)),
                            actionType = TimeListActionType.BREAK,
                            order = null
                        ),
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 30)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH).plusHours(3),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null
                        ),
                    )
                ),
            )

        ServiceTestUtil.assertOrderTimeList(
            actual.sortedBy { it.employee.id },
            expected.sortedBy { it.employee.id })
    }

    @Test
    fun test_getOrderTimeListWithAllTime() {
        // mock
        val date = LocalDate.of(2024, 1, 1)
        val instantDate = date.atStartOfDay(TIME_ZONE_UTC).toInstant()

        val employee1 = ServiceTestUtil.makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )

        val employeeShift =
            makeEmployeeShiftEntity(
                id = 111,
                shiftDate = date,
                workStart = LocalTime.of(9, 0),
                workFinish = LocalTime.of(18, 0),
                employee = employee1,
            )
        Mockito.`when`(employeeShiftEntityRepository.findAllByShiftDate(instantDate))
            .thenReturn(listOf(employeeShift))

        Mockito.`when`(employeeShiftOrderEntityRepository.findAllByEmployeeShiftId(111))
            .thenReturn(
                listOf(
                    makeEmployeeShiftOrderEntity(
                        id = 1,
                        date = date,
                        timeStart = LocalTime.of(9, 0),
                        timeFinish = LocalTime.of(10, 0),
                        employeeShift = employeeShift,
                        actionType = TimeListActionType.BREAK,
                        isAttached = true
                    ),
                    makeEmployeeShiftOrderEntity(
                        id = 2,
                        date = date,
                        timeStart = LocalTime.of(11, 0),
                        timeFinish = LocalTime.of(12, 0),
                        employeeShift = employeeShift,
                        actionType = TimeListActionType.BREAK,
                        isAttached = true
                    ),
                )
            )

        // act
        val result: OrderTimeListDTO = service.getOrderTimeListWithAllTimeForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        assertTrue(actualOrdersNotInPlan.isEmpty())
        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START).plusHours(3),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH).plusHours(3),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                    )
                )
            )
        ServiceTestUtil.assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun test_getOrderTimeList() {
        // mock
        val date = LocalDate.of(2024, 1, 1)
        val instantDate = date.atStartOfDay(TIME_ZONE_UTC).toInstant()

        val employee1 = ServiceTestUtil.makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )

        val employeeShift =
            makeEmployeeShiftEntity(
                id = 111,
                shiftDate = date,
                workStart = LocalTime.of(9, 0),
                workFinish = LocalTime.of(18, 0),
                employee = employee1,
            )
        Mockito.`when`(employeeShiftEntityRepository.findAllByShiftDate(instantDate))
            .thenReturn(listOf(employeeShift))

        Mockito.`when`(employeeShiftOrderEntityRepository.findAllByEmployeeShiftId(111))
            .thenReturn(
                listOf(
                    makeEmployeeShiftOrderEntity(
                        id = 1,
                        date = date,
                        timeStart = LocalTime.of(9, 0),
                        timeFinish = LocalTime.of(10, 0),
                        employeeShift = employeeShift,
                        actionType = TimeListActionType.BREAK,
                        isAttached = true
                    ),
                    makeEmployeeShiftOrderEntity(
                        id = 2,
                        date = date,
                        timeStart = LocalTime.of(11, 0),
                        timeFinish = LocalTime.of(12, 0),
                        employeeShift = employeeShift,
                        actionType = TimeListActionType.BREAK,
                        isAttached = true
                    ),
                    makeEmployeeShiftOrderEntity(
                        id = 3,
                        date = date,
                        timeStart = LocalTime.of(13, 0),
                        timeFinish = LocalTime.of(14, 0),
                        employeeShift = employeeShift,
                        actionType = TimeListActionType.BREAK,
                        isAttached = false
                    ),
                )
            )

        // act
        val actualOrdersTime: List<OrderTime> = runBlocking { service.getOrderTimeList(date) }

        // assert
        val expectedOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(9, 0),
                            timeFinish = LocalTime.of(10, 0),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                        makeEmployeeShiftOrder(
                            date = date,
                            timeStart = LocalTime.of(11, 0),
                            timeFinish = LocalTime.of(12, 0),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        ),
                    )
                )
            )
        ServiceTestUtil.assertOrderTimeList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }


    fun makeEmployeeShiftEntity(
        id: Long,
        shiftDate: LocalDate,
        workStart: LocalTime,
        workFinish: LocalTime,
        employee: Employee
    ): EmployeeShiftEntity {
        val instantShiftDate = shiftDate.atStartOfDay(TIME_ZONE_UTC).toInstant()
        return EmployeeShiftEntity(
            id = id,
            shiftDate = instantShiftDate,
            workStart = workStart.minusHours(3),
            workFinish = workFinish.minusHours(3),
            employee = employeeMapper.domainToEntity(employee, MetroUserEntity(1, "", "", false)),
        )
    }

    fun makeEmployeeShiftOrderEntity(
        id: Long,
        date: LocalDate,
        timeStart: LocalTime,
        timeFinish: LocalTime,
        employeeShift: EmployeeShiftEntity,
        actionType: TimeListActionType,
        isAttached: Boolean
    ): EmployeeShiftOrderEntity {
        return EmployeeShiftOrderEntity(
            id = id,
            employeeShift = employeeShift,
            order = null,
            isAttached = isAttached,
            actionType = actionType.name,
            timeStart = LocalDateTime.of(date, timeStart).minusHours(3).toInstant(ZoneOffset.UTC),
            timeFinish = LocalDateTime.of(date, timeFinish).minusHours(3).toInstant(ZoneOffset.UTC),
            createdAt = Instant.now(),
        )
    }

    fun makeEmployeeShiftOrder(
        date: LocalDate,
        timeStart: LocalTime,
        timeFinish: LocalTime,
        actionType: TimeListActionType,
        order: PassengerOrder?,
    ): EmployeeShiftOrder {
        return EmployeeShiftOrder(
            timeStart = LocalDateTime.of(date, timeStart).minusHours(3),
            timeFinish = LocalDateTime.of(date, timeFinish).minusHours(3),
            actionType = actionType,
            order = order,
        )
    }

    fun makeEmployeeShiftOrder(
        timeStart: LocalDateTime,
        timeFinish: LocalDateTime,
        actionType: TimeListActionType,
        order: PassengerOrder?,
    ): EmployeeShiftOrder {
        return EmployeeShiftOrder(
            timeStart = timeStart.minusHours(3),
            timeFinish = timeFinish.minusHours(3),
            actionType = actionType,
            order = order,
        )
    }

    private fun makeOrderTimeDTO(
        employee: Employee,
        actions: List<EmployeeShiftOrderDTO>
    ): OrderTimeDTO {
        return OrderTimeDTO(
            employeeMapper.domainToDto(employee),
            actions
        )
    }

    private fun makeEmployeeShiftOrderDTO(
        timeStart: LocalDateTime,
        timeFinish: LocalDateTime,
        actionType: TimeListActionType,
        order: PassengerOrder?,
    ): EmployeeShiftOrderDTO {
        return EmployeeShiftOrderDTO(
            timeStart = timeStart.toInstant(TIME_ZONE_MOSCOW),
            timeEnd = timeFinish.toInstant(TIME_ZONE_MOSCOW),
            actionType = actionType,
            order = order?.let { orderMapper.domainToDto(order) },
        )
    }
}
