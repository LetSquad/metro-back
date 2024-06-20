package ru.mosmetro.backend.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import ru.mosmetro.backend.mapper.EmployeeMapper
import ru.mosmetro.backend.mapper.EmployeeShiftOrderMapper
import ru.mosmetro.backend.mapper.OrderMapper
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.domain.MetroStation
import ru.mosmetro.backend.model.domain.OrderTime
import ru.mosmetro.backend.model.domain.PassengerCategory
import ru.mosmetro.backend.model.domain.PassengerOrder
import ru.mosmetro.backend.model.dto.order.EmployeeShiftOrderDTO
import ru.mosmetro.backend.model.dto.order.OrderTimeDTO
import ru.mosmetro.backend.model.dto.order.PassengerOrderDTO
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.model.enums.PassengerCategoryType
import ru.mosmetro.backend.model.enums.SexType
import ru.mosmetro.backend.model.enums.TimeListActionType
import ru.mosmetro.backend.service.ServiceTestUtil.assertOrderTimeDTOList
import ru.mosmetro.backend.service.ServiceTestUtil.makeEmployee
import ru.mosmetro.backend.service.ServiceTestUtil.makeEmployeeShiftOrder
import ru.mosmetro.backend.service.ServiceTestUtil.makeMetroStation
import ru.mosmetro.backend.service.ServiceTestUtil.makeOrderBaggage
import ru.mosmetro.backend.service.ServiceTestUtil.makeOrderTime
import ru.mosmetro.backend.service.ServiceTestUtil.makePassengerOrder
import ru.mosmetro.backend.util.MetroTimeUtil.METRO_TIME_FINISH
import ru.mosmetro.backend.util.MetroTimeUtil.METRO_TIME_START
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class OrderDistributionServiceTest {

    @Autowired
    lateinit var employeeMapper: EmployeeMapper

    @Autowired
    lateinit var orderMapper: OrderMapper

    @Autowired
    lateinit var employeeShiftOrderMapper: EmployeeShiftOrderMapper


    @SpyBean
    lateinit var timeListService: TimeListService

    @MockBean
    lateinit var orderService: OrderService

    @MockBean
    lateinit var metroTransfersService: MetroTransfersService

    @MockBean
    lateinit var breakTimeGuesserService: BreakTimeGuesserService

    @Test
    fun WHEN_employee_1m_0f_AND_order_3() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
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
                finishMetroStation = station2,
            )
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station4,
            )
        val order3 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(14, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station4,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(

            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order1,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 55)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 55)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            actionType = TimeListActionType.TRANSFER,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order2,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 45)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 45)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            actionType = TimeListActionType.TRANSFER,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order3,
                        ),


                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_1m_0f_AND_order_3_AND_all_in_nigth_shift() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(20, 0),
            workFinish = LocalTime.of(8, 0),
            lightDuties = false,
        )
        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(21, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(22, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station2,
            )
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(23, 30)),
                finishTime = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 30)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station4,
            )
        val order3 =
            makePassengerOrder(
                id = 3,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 30)),
                finishTime = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 45)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station4,
                finishMetroStation = station4,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(

            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(20, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(20, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(21, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(21, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(22, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order1,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(22, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(23, 25)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(23, 25)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(23, 30)),
                            actionType = TimeListActionType.TRANSFER,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(23, 30)),
                            timeFinish = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 30)),
                            actionType = TimeListActionType.ORDER,
                            order = order2,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 30)),
                            timeFinish = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 45)),
                            actionType = TimeListActionType.ORDER,
                            order = order3,
                        ),


                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 45)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_1m_0f_AND_order_3_AND_orders_3_parallel_AND_orders_created_in_diff_times() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                createdAt = LocalDateTime.of(date, LocalTime.of(0, 15)),
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
                finishMetroStation = station2,
            )
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                createdAt = LocalDateTime.of(date, LocalTime.of(0, 10)),
                orderTime = LocalDateTime.of(date, LocalTime.of(10, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(11, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station4,
            )
        val order3 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                createdAt = LocalDateTime.of(date, LocalTime.of(0, 5)),
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
                finishMetroStation = station4,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(
                orderMapper.domainToDto(order1),
                orderMapper.domainToDto(order2)
            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order3,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_1m_0f_AND_order_3_AND_order_2_parallel() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
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
                finishMetroStation = station2,
            )
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station4,
            )
        val order3 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(13, 5)),
                finishTime = LocalDateTime.of(date, LocalTime.of(14, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station4,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(
                orderMapper.domainToDto(order3)
            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order1,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 55)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 55)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            actionType = TimeListActionType.TRANSFER,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order2,
                        ),


                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_1m_0f_AND_order_3_AND_order_2_parallel_BUT_o3_created_early() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
                id = 1,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                createdAt = LocalDateTime.of(date, LocalTime.of(0, 30)),
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
                finishMetroStation = station2,
            )
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                createdAt = LocalDateTime.of(date, LocalTime.of(0, 20)),
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station4,
            )
        val order3 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                createdAt = LocalDateTime.of(date, LocalTime.of(0, 10)),
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station4,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(
                orderMapper.domainToDto(order2)
            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order1,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 55)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 55)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            actionType = TimeListActionType.TRANSFER,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order3,
                        ),


                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_1m_0f_AND_order_3_AND_timeShift_invalid() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(10, 0),
            lightDuties = false,
        )

        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
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
                finishMetroStation = station2,
            )
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station2,
                finishMetroStation = station3,
            )
        val order3 =
            makePassengerOrder(
                id = 3,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(14, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station4,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(
                orderMapper.domainToDto(order1),
                orderMapper.domainToDto(order2),
                orderMapper.domainToDto(order3),
            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_1m_0f_AND_order_3_AND_orderNeedFemale() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )

        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
                id = 1,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
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
                finishMetroStation = station2,
            )
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station2,
                finishMetroStation = station3,
            )
        val order3 =
            makePassengerOrder(
                id = 3,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(14, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station4,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(

            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order1,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order2,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order3,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_1m_0f_AND_employee_ligth_AND_order_3_AND_orderNeedBaggageHelp() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = true,
        )

        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
                id = 1,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(10, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(11, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = makeOrderBaggage(
                    isHelpNeeded = true,
                ),
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station1,
                finishMetroStation = station2,
            )
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station2,
                finishMetroStation = station3,
            )
        val order3 =
            makePassengerOrder(
                id = 3,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(14, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = makeOrderBaggage(
                    isHelpNeeded = true,
                ),
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station4,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(
                orderMapper.domainToDto(order1),
                orderMapper.domainToDto(order3),
            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order2,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_1m_1f_AND_order_4_AND_order13_male_order24_female() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val employee2 = makeEmployee(
            id = 2,
            sex = SexType.FEMALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )

        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                ),
                // plan empty
                makeOrderTime(
                    employee2,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
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
                finishMetroStation = station2,
            )
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station2,
                finishMetroStation = station3,
            )
        val order3 =
            makePassengerOrder(
                id = 3,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(14, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station4,
            )
        val order4 =
            makePassengerOrder(
                id = 4,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(16, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(17, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station4,
                finishMetroStation = station1,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3, order4)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(
            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order1,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 55)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 55)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            actionType = TimeListActionType.TRANSFER,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order3,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                ),
                makeOrderTimeDTO(
                    employee2,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order2,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(15, 55)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(15, 55)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(16, 0)),
                            actionType = TimeListActionType.TRANSFER,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(16, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(17, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order4,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(17, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_2m_0f_AND_order_3_AND_order3_get_nearest() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val employee2 = makeEmployee(
            id = 2,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )

        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf()
                ),
                // plan empty
                makeOrderTime(
                    employee2,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
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
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station4,
                finishMetroStation = station4,
            )
        val order3 =
            makePassengerOrder(
                id = 3,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(14, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station3,
            )
        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(
            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order1,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                ),
                makeOrderTimeDTO(
                    employee2,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order2,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 55)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 55)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            actionType = TimeListActionType.TRANSFER,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order3,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_2m_0f_AND_order_3_AND_order3_get_nearest_to_empl2_BUT_empl1_attached_to_order3() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val employee2 = makeEmployee(
            id = 2,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
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
        val order2 =
            makePassengerOrder(
                id = 2,
                maleEmployeeCount = 0,
                femaleEmployeeCount = 1,
                orderTime = LocalDateTime.of(date, LocalTime.of(12, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(13, 0)),
                orderStatusType = OrderStatusType.WAITING_LIST,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station4,
                finishMetroStation = station4,
            )
        val order3 =
            makePassengerOrder(
                id = 3,
                maleEmployeeCount = 1,
                femaleEmployeeCount = 0,
                orderTime = LocalDateTime.of(date, LocalTime.of(14, 0)),
                finishTime = LocalDateTime.of(date, LocalTime.of(15, 0)),
                orderStatusType = OrderStatusType.ACCEPTED,
                baggage = null,
                passengerCategory = PassengerCategory(
                    code = PassengerCategoryType.PL,
                    name = "",
                    shortName = ""
                ),
                startMetroStation = station3,
                finishMetroStation = station3,
            )
        // TODO без фильтрации заявок по статусу order3 не должен возвращаться в списке на распределение
        val orders: List<PassengerOrder> = listOf(order1, order2)
//        val orders: List<PassengerOrder> = listOf(order1, order2, order3)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        val orderTimeDB: List<OrderTime> =
            listOf(
                makeOrderTime(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 50)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            actionType = TimeListActionType.TRANSFER,
                            order = order1,
                        ),
                        makeEmployeeShiftOrder(
                            timeStart = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order3,
                        )
                    )
                ),
                // plan empty
                makeOrderTime(
                    employee2,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(
            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order1,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 50)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 50)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            actionType = TimeListActionType.TRANSFER,
                            order = order1,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(14, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order3,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(15, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                ),
                makeOrderTimeDTO(
                    employee2,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(12, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order2,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(13, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
    }

    @Test
    fun WHEN_employee_3m_0f_AND_order_1_THEN_first_in_list() {
        // mock
        val date = LocalDate.of(2024, 1, 1)

        val employee1 = makeEmployee(
            id = 1,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val employee2 = makeEmployee(
            id = 2,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )
        val employee3 = makeEmployee(
            id = 2,
            sex = SexType.MALE,
            workStart = LocalTime.of(9, 0),
            workFinish = LocalTime.of(18, 0),
            lightDuties = false,
        )

        // 1 --300--> 2 --300--> 3 --300--> 4
        val station1 = makeMetroStation("station-1")
        val station2 = makeMetroStation("station-2")
        val station3 = makeMetroStation("station-3")
        val station4 = makeMetroStation("station-4")
        mockSimpleMetroLine(station1, station2, station3, station4)

        val order1 =
            makePassengerOrder(
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
        val orders: List<PassengerOrder> = listOf(order1)

        val orderStartTime = LocalDateTime.of(date, METRO_TIME_START)
        val orderFinishTime = LocalDateTime.of(date, METRO_TIME_FINISH)
        mockGetEmployeeTimePlanDB(orderStartTime, orderFinishTime, orders)

        val orderTimeDB: List<OrderTime> =
            listOf(
                // plan empty
                makeOrderTime(
                    employee1,
                    listOf(
                    )
                ),
                // plan empty
                makeOrderTime(
                    employee2,
                    listOf()
                ),
                // plan empty
                makeOrderTime(
                    employee3,
                    listOf()
                )
            )
        mockGetEmployeeTImePlanDB(date, orderTimeDB)

        // act
        val service = getService()
        var result = service.calculateOrderDistributionForTest(date)
        val actualOrdersTime: List<OrderTimeDTO> = result.ordersTime
        val actualOrdersNotInPlan: List<PassengerOrderDTO> = result.ordersNotInPlan

        // assert
        val expectedOrdersNotInPlan: List<PassengerOrderDTO> =
            listOf(
            )
        assertEquals(expectedOrdersNotInPlan.size, actualOrdersNotInPlan.size)
        assertTrue(expectedOrdersNotInPlan.containsAll(actualOrdersNotInPlan))
        assertTrue(actualOrdersNotInPlan.containsAll(expectedOrdersNotInPlan))

        val expectedOrdersTime: List<OrderTimeDTO> =
            listOf(
                makeOrderTimeDTO(
                    employee1,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(10, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            actionType = TimeListActionType.ORDER,
                            order = order1,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(11, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                ),
                makeOrderTimeDTO(
                    employee2,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                ),
                makeOrderTimeDTO(
                    employee3,
                    listOf(
                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, METRO_TIME_START),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(9, 0)),
                            timeFinish = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            actionType = TimeListActionType.DOWNTIME,
                            order = null,
                        ),

                        makeEmployeeShiftOrderDTO(
                            timeStart = LocalDateTime.of(date, LocalTime.of(18, 0)),
                            timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                            actionType = TimeListActionType.NON_WORKING,
                            order = null,
                        ),
                    )
                )
            )
        assertOrderTimeDTOList(
            actualOrdersTime.sortedBy { it.employee.id },
            expectedOrdersTime.sortedBy { it.employee.id })
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
            timeStart = timeStart.toInstant(ZoneOffset.of("+03:00")),
            timeEnd = timeFinish.toInstant(ZoneOffset.of("+03:00")),
            actionType = actionType,
            order = order?.let { orderMapper.domainToDto(order) },
        )
    }

    private fun mockSimpleMetroLine(
        station1: MetroStation,
        station2: MetroStation,
        station3: MetroStation,
        station4: MetroStation,
    ) {
        mockTransferDuration(station1, station1, 0)
        mockTransferDuration(station2, station2, 0)
        mockTransferDuration(station3, station3, 0)
        mockTransferDuration(station4, station4, 0)
        mockTransferDuration(station1, station2, 300)
        mockTransferDuration(station1, station3, 600)
        mockTransferDuration(station1, station4, 900)
        mockTransferDuration(station2, station3, 300)
        mockTransferDuration(station2, station4, 600)
        mockTransferDuration(station3, station4, 300)
    }

    private fun mockTransferDuration(
        station1: MetroStation,
        station2: MetroStation,
        seconds: Long
    ) {
        Mockito.`when`(metroTransfersService.calculateMetroStationTransfersDuration(station1, station2))
            .thenReturn(seconds)
        Mockito.`when`(metroTransfersService.calculateMetroStationTransfersDuration(station2, station1))
            .thenReturn(seconds)
    }

    private fun mockGetEmployeeTImePlanDB(
        onDate: LocalDate,
        list: List<OrderTime>
    ) = runBlocking {
        Mockito.`when`(timeListService.getOrderTimeList(onDate)).thenReturn(list)
    }

    private fun mockGetEmployeeTimePlanDB(
        orderStartTime: LocalDateTime,
        orderFinishTime: LocalDateTime,
        list: List<PassengerOrder>
    ) {
        Mockito.`when`(orderService.getOrdersBetweenOrderDate(orderStartTime, orderFinishTime))
            .thenReturn(list)
    }


    private fun getService() = OrderDistributionService(
        timeListService,
        orderService,
        metroTransfersService,
        breakTimeGuesserService,

        employeeMapper,
        orderMapper,
        employeeShiftOrderMapper
    )

}
