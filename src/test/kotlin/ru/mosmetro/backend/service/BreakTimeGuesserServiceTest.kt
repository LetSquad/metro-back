package ru.mosmetro.backend.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.mosmetro.backend.model.domain.OrderTime
import ru.mosmetro.backend.model.domain.PassengerCategory
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.model.enums.PassengerCategoryType
import ru.mosmetro.backend.model.enums.SexType
import ru.mosmetro.backend.model.enums.TimeListActionType
import ru.mosmetro.backend.service.ServiceTestUtil.makeEmployeeShiftOrder
import ru.mosmetro.backend.service.ServiceTestUtil.makeOrderTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@SpringBootTest
class BreakTimeGuesserServiceTest {

    @Autowired
    lateinit var service: BreakTimeGuesserService

    @Test
    fun WHEN_employee_has_break_THEN_no_guess() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val station1 = ServiceTestUtil.makeMetroStation("station-1")

        val employee1 = ServiceTestUtil.makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val order1 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(10, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(11, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )

        val actualOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(1, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(2, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = order1,
                        )
                    )
                )
            )

        // act
        service.guessBreakTime(
            date,
            actualOrdersTime,
            listOf(
                order1
            ),
            false
        )

        // assert
        val expectedOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(1, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(2, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = order1,
                        )
                    )
                )
            )

        ServiceTestUtil.assertOrderTimeList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_has_no_break_AND_has_nigth_shift_THEN_break_when_metro_stop() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val station1 = ServiceTestUtil.makeMetroStation("station-1")

        val employee1 = ServiceTestUtil.makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(20, 0),
            workFinish = LocalTime.of(8, 0),
            lightDuties = false,
        )
        val order1 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(10, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(11, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )

        val actualOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                    )
                )
            )

        // act
        service.guessBreakTime(
            date,
            actualOrdersTime,
            listOf(
                order1
            ),
            false
        )

        // assert
        val expectedOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            // TimeZone MOS = -3h
                            timeStart = LocalDateTime.of(date.plusDays(1), LocalTime.of(6, 0)),
                            timeFinish = LocalDateTime.of(date.plusDays(1), LocalTime.of(7, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        )
                    )
                )
            )

        ServiceTestUtil.assertOrderTimeList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_has_no_break_AND_has_free_time_between_orders_THEN_break_when_time_free_between_orders() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val station1 = ServiceTestUtil.makeMetroStation("station-1")

        val employee1 = ServiceTestUtil.makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val order1 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(9, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )
        val order2 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 30)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )
        val order3 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(14, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(18, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )

        val actualOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                    )
                )
            )

        // act
        service.guessBreakTime(
            date,
            actualOrdersTime,
            listOf(
                order1,
                order2,
                order3
            ),
            false
        )

        // assert
        val expectedOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        )
                    )
                )
            )

        ServiceTestUtil.assertOrderTimeList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_2employee_has_no_break_AND_has_free_employee_between_work_THEN_break_when_time_free() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val station1 = ServiceTestUtil.makeMetroStation("station-1")

        val employee1 = ServiceTestUtil.makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val employee2 = ServiceTestUtil.makeEmployee(
            id = 2,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val order1 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(9, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )
        val order2 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 30)),
                finishTime = LocalDateTime.of(date, LocalTime.of(15, 30)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )
        val order3 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(18, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )

        val actualOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                    )
                ),
                makeOrderTime(
                    employee2,
                    listOf(
                    )
                ),
            )

        // act
        service.guessBreakTime(
            date,
            actualOrdersTime,
            listOf(
                order1,
                order2,
                order3
            ),
            false
        )

        // assert
        val expectedOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        )
                    )
                ),
                makeOrderTime(
                    employee2,
                    listOf(
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(16, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(17, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        )
                    )
                ),
            )

        ServiceTestUtil.assertOrderTimeList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_2employee_has_no_break_AND_has_no_free_employee_between_work() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val station1 = ServiceTestUtil.makeMetroStation("station-1")

        val employee1 = ServiceTestUtil.makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val employee2 = ServiceTestUtil.makeEmployee(
            id = 2,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val order1 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(9, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )
        val order2 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )
        val order3 =
            ServiceTestUtil.makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(18, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station1,
            )

        val actualOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                    )
                ),
                makeOrderTime(
                    employee2,
                    listOf(
                    )
                ),
            )

        // act
        service.guessBreakTime(
            date,
            actualOrdersTime,
            listOf(
                order1,
                order2,
                order3
            ),
            false
        )

        // assert
        val expectedOrdersTime: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        )
                    )
                ),
                makeOrderTime(
                    employee2,
                    listOf(
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(16, 0)),
                            actionType = TimeListActionType.BREAK,
                            order = null,
                        )
                    )
                ),
            )

        ServiceTestUtil.assertOrderTimeList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }
}