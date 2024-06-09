package ru.mosmetro.backend.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.mosmetro.backend.mapper.MetroStationMapper
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.domain.EmployeeRank
import ru.mosmetro.backend.model.domain.MetroStation
import ru.mosmetro.backend.model.domain.Order
import ru.mosmetro.backend.model.enums.OrderStatusType
import ru.mosmetro.backend.model.enums.SexType
import ru.mosmetro.backend.repository.MetroStationEntityRepository
import ru.mosmetro.backend.service.OrdersDistributionServiceUtil.Companion.KRYLATSKOE_STATION_ID
import ru.mosmetro.backend.service.OrdersDistributionServiceUtil.Companion.MIYAKININO_STATION_ID
import ru.mosmetro.backend.service.OrdersDistributionServiceUtil.Companion.MOLODEGNAY_STATION_ID
import ru.mosmetro.backend.service.OrdersDistributionServiceUtil.Companion.PARK_POBEDY_STATION_ID
import ru.mosmetro.backend.service.OrdersDistributionServiceUtil.Companion.PYATNITSKOE_SHOSSE_STATION_ID
import ru.mosmetro.backend.service.OrdersDistributionServiceUtil.Companion.SLOVYANSKY_BULVAR_STATION_ID
import ru.mosmetro.backend.service.OrdersDistributionServiceUtil.Companion.STROGINO_STATION_ID
import ru.mosmetro.backend.service.OrdersDistributionServiceUtil.Companion.VOLOKOLAMSKAY_STATION_ID
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertEquals

@SpringBootTest
class OrdersDistributionServiceTest {

    @Autowired
    lateinit var service: OrdersDistributionService

    @Autowired
    lateinit var metroStationEntityRepository: MetroStationEntityRepository

    @Autowired
    lateinit var metroStationMapper: MetroStationMapper

    // 1 работник
    // 3 заявки подряд
    // происходит распределение
    @Test
    fun test1() {
        val empl1 =
            createEmployee(1, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(23, 59))
        val employeeList = listOf(empl1)

        val order1 =
            createOrder(
                1,
                PYATNITSKOE_SHOSSE_STATION_ID, VOLOKOLAMSKAY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 1, 0, 0),
                0,
                1,
                1
            )
        val order2 =
            createOrder(
            2,
            STROGINO_STATION_ID, MOLODEGNAY_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 2, 0, 0),
            LocalDateTime.of(2024, 1, 1, 4, 0, 0),
            0,
            1,
            1
        )
        val order3 =
            createOrder(
                3,
                SLOVYANSKY_BULVAR_STATION_ID, PARK_POBEDY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 6, 0, 0),
                LocalDateTime.of(2024, 1, 1, 8, 0, 0),
                0,
                1,
                1
            )
        val orderList = listOf(order1, order2, order3)

        val resultMap: Map<Order, List<Employee>> = service.autoDistribution(employeeList, orderList, LocalDate.of(2024, 1, 1))

        val expectedMap = mapOf(
            Pair(order1, listOf(empl1)),
            Pair(order2, listOf(empl1)),
            Pair(order3, listOf(empl1)),
        )

        assertResultMap(expectedMap, resultMap)
    }

    // 1 работник
    // 2 заявки подряд, 1 пересекается и выкидывается
    // заявки на которых нет сотрудников выкидываются
    @Test
    fun test2() {
        val empl1 = createEmployee(1, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(23, 59))
        val employeeList = listOf(empl1)

        val order1 =
            createOrder(
                1,
                PYATNITSKOE_SHOSSE_STATION_ID, VOLOKOLAMSKAY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 2, 0, 0),
                0,
                1,
                1
            )
        val order2 =
            createOrder(
            2,
            STROGINO_STATION_ID, MOLODEGNAY_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 2, 10, 0),
            LocalDateTime.of(2024, 1, 1, 4, 0, 0),
            0,
            1,
            1
        )
        val order3 =
            createOrder(
                3,
                SLOVYANSKY_BULVAR_STATION_ID, PARK_POBEDY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 1, 3, 0, 0),
                0,
                1,
                1
            )
        val orderList = listOf(order1, order2, order3)

        val resultMap: Map<Order, List<Employee>> = service.autoDistribution(employeeList, orderList, LocalDate.of(2024, 1, 1))

        val expectedMap = mapOf(
            Pair(order1, listOf(empl1)),
            Pair(order2, listOf(empl1)),
        )

        assertResultMap(expectedMap, resultMap)
    }

    // 2 работника ж и м
    // 3 заявки подряд
    // заявки распределяюься по полу сотрудников
    @Test
    fun test3() {
        val empl1 = createEmployee(1, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(23, 59))
        val empl2 = createEmployee(2, SexType.FEMALE, LocalTime.of(0, 0), LocalTime.of(23, 59))
        val employeeList = listOf(empl1, empl2)

        val order1 =
            createOrder(
                1,
                PYATNITSKOE_SHOSSE_STATION_ID, VOLOKOLAMSKAY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 2, 0, 0),
                0,
                1,
                1
            )
        val order2 =
            createOrder(
            2,
            STROGINO_STATION_ID, MOLODEGNAY_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 2, 10, 0),
            LocalDateTime.of(2024, 1, 1, 4, 0, 0),
            0,
            1,
            1
        )
        val order3 =
            createOrder(
                3,
                SLOVYANSKY_BULVAR_STATION_ID, PARK_POBEDY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 6, 0, 0),
                LocalDateTime.of(2024, 1, 1, 8, 0, 0),
                1,
                1,
                1
            )
        val orderList = listOf(order1, order2, order3)

        val resultMap: Map<Order, List<Employee>> = service.autoDistribution(employeeList, orderList, LocalDate.of(2024, 1, 1))

        val expectedMap = mapOf(
            Pair(order1, listOf(empl1)),
            Pair(order2, listOf(empl1)),
            Pair(order3, listOf(empl1, empl2)),
        )

        assertResultMap(expectedMap, resultMap)
    }

    // 2 работника ж и м
    // 4 заявки подряд = 2 ж и 2 м
    // заявки распределяюься по полу сотрудников
    @Test
    fun test4() {
        val empl1 = createEmployee(1, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(23, 59))
        val empl2 = createEmployee(2, SexType.FEMALE, LocalTime.of(0, 0), LocalTime.of(23, 59))
        val employeeList = listOf(empl1, empl2)

        val order1 =
            createOrder(
                1,
                PYATNITSKOE_SHOSSE_STATION_ID, VOLOKOLAMSKAY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 3, 0, 0),
                1,
                0,
                1
            )
        val order2 =
            createOrder(
            2,
            STROGINO_STATION_ID, MOLODEGNAY_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 2, 0, 0),
            LocalDateTime.of(2024, 1, 1, 4, 0, 0),
            0,
            1,
            1
        )
        val order3 =
            createOrder(
                3,
                SLOVYANSKY_BULVAR_STATION_ID, PARK_POBEDY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 5, 0, 0),
                LocalDateTime.of(2024, 1, 1, 7, 0, 0),
                1,
                0,
                1
            )
        val order4 =
            createOrder(
                4,
                SLOVYANSKY_BULVAR_STATION_ID, PARK_POBEDY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 6, 0, 0),
                LocalDateTime.of(2024, 1, 1, 8, 0, 0),
                0,
                1,
                1
            )
        val orderList = listOf(order1, order2, order3, order4)

        val resultMap: Map<Order, List<Employee>> = service.autoDistribution(employeeList, orderList, LocalDate.of(2024, 1, 1))

        val expectedMap = mapOf(
            Pair(order1, listOf(empl2)),
            Pair(order2, listOf(empl1)),
            Pair(order3, listOf(empl2)),
            Pair(order4, listOf(empl1)),
        )

        assertResultMap(expectedMap, resultMap)
    }

    // 2 работника м
    // 4 заявки подряд
    // работает фильтр по времени работы сотрудника
    @Test
    fun test5() {
        val empl1 = createEmployee(1, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(12, 0))
        val empl2 = createEmployee(2, SexType.MALE, LocalTime.of(12, 0), LocalTime.of(23, 59))
        val employeeList = listOf(empl1, empl2)

        val order1 =
            createOrder(
                1,
                PYATNITSKOE_SHOSSE_STATION_ID, VOLOKOLAMSKAY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 6, 0, 0),
                0,
                1,
                1
            )
        val order2 =
            createOrder(
            2,
            STROGINO_STATION_ID, MOLODEGNAY_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 6, 10, 0),
            LocalDateTime.of(2024, 1, 1, 12, 0, 0),
            0,
            1,
            1
        )
        val order3 =
            createOrder(
                3,
                SLOVYANSKY_BULVAR_STATION_ID, PARK_POBEDY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 12, 0, 0),
                LocalDateTime.of(2024, 1, 1, 18, 0, 0),
                0,
                1,
                1
            )
        val order4 =
            createOrder(
                4,
                SLOVYANSKY_BULVAR_STATION_ID, PARK_POBEDY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 18, 10, 0),
                LocalDateTime.of(2024, 1, 1, 23, 0, 0),
                0,
                1,
                1
            )
        val orderList = listOf(order1, order2, order3, order4)

        val resultMap: Map<Order, List<Employee>> = service.autoDistribution(employeeList, orderList, LocalDate.of(2024, 1, 1))

        val expectedMap = mapOf(
            Pair(order1, listOf(empl1)),
            Pair(order2, listOf(empl1)),
            Pair(order3, listOf(empl2)),
            Pair(order4, listOf(empl2)),
        )

        assertResultMap(expectedMap, resultMap)
    }

    // 1 работник м
    // 2 заявки подряд
    // если нет ж - то берутся м
    @Test
    fun test6() {
        val empl1 = createEmployee(1, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(12, 0))
        val employeeList = listOf(empl1)

        val order1 =
            createOrder(
                1,
                PYATNITSKOE_SHOSSE_STATION_ID, VOLOKOLAMSKAY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 6, 0, 0),
                1,
                0,
                1
            )
        val order2 =
            createOrder(
            2,
            STROGINO_STATION_ID, MOLODEGNAY_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 6, 10, 0),
            LocalDateTime.of(2024, 1, 1, 12, 0, 0),
            1,
            0,
            1
        )
        val orderList = listOf(order1, order2)

        val resultMap: Map<Order, List<Employee>> = service.autoDistribution(employeeList, orderList, LocalDate.of(2024, 1, 1))

        val expectedMap = mapOf(
            Pair(order1, listOf(empl1)),
            Pair(order2, listOf(empl1)),
        )

        assertResultMap(expectedMap, resultMap)
    }

    // 2 работника
    // 4 заявки параллельно
    // кто ближе - тот берет
    @Test
    fun test7() {
        val empl1 = createEmployee(1, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(23, 0))
        val empl2 = createEmployee(2, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(23, 0))
        val employeeList = listOf(empl1, empl2)

        val order1 =
            createOrder(
                1,
                PYATNITSKOE_SHOSSE_STATION_ID, VOLOKOLAMSKAY_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 2, 0, 0),
                0,
                1,
                1
            )
        val order2 =
            createOrder(
            2,
                PYATNITSKOE_SHOSSE_STATION_ID, STROGINO_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 0, 0, 0),
            LocalDateTime.of(2024, 1, 1, 2, 0, 0),
            0,
            1,
            1
        )
        val order3 =
            createOrder(
            3,
                KRYLATSKOE_STATION_ID, SLOVYANSKY_BULVAR_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 4, 0, 0),
            LocalDateTime.of(2024, 1, 1, 6, 0, 0),
            0,
            1,
            1
        )
        val order4 =
            createOrder(
            4,
                MIYAKININO_STATION_ID, MOLODEGNAY_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 4, 0, 0),
            LocalDateTime.of(2024, 1, 1, 6, 0, 0),
            0,
            1,
            1
        )
        val orderList = listOf(order1, order2, order4, order3)

        val resultMap: Map<Order, List<Employee>> = service.autoDistribution(employeeList, orderList, LocalDate.of(2024, 1, 1))

        val expectedMap = mapOf(
            Pair(order1, listOf(empl1)),
            Pair(order2, listOf(empl2)),
            Pair(order3, listOf(empl1)),
            Pair(order4, listOf(empl2)),
        )

        assertResultMap(expectedMap, resultMap)
    }

    // 2 работника
    // 4 заявки параллельно
    // кто ближе - тот берет
    @Test
    fun test8() {
        val empl1 = createEmployee(1, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(23, 0))
        val empl2 = createEmployee(2, SexType.MALE, LocalTime.of(0, 0), LocalTime.of(23, 0))
        val employeeList = listOf(empl1, empl2)

        val order1 =
            createOrder(
                1,
                PYATNITSKOE_SHOSSE_STATION_ID, MIYAKININO_STATION_ID,
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 1, 2, 0, 0),
                0,
                1,
                1
            )
        val order2 =
            createOrder(
            2,
                PYATNITSKOE_SHOSSE_STATION_ID, KRYLATSKOE_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 0, 0, 0),
            LocalDateTime.of(2024, 1, 1, 2, 0, 0),
            0,
            1,
            1
        )
        val order3 =
            createOrder(
            3,
                STROGINO_STATION_ID, SLOVYANSKY_BULVAR_STATION_ID,
            LocalDateTime.of(2024, 1, 1, 2, 4, 0),
            LocalDateTime.of(2024, 1, 1, 4, 0, 0),
            0,
            1,
            1
        )
        val orderList = listOf(order1, order2, order3)

        val resultMap: Map<Order, List<Employee>> = service.autoDistribution(employeeList, orderList, LocalDate.of(2024, 1, 1))

        val expectedMap = mapOf(
            Pair(order1, listOf(empl1)),
            Pair(order2, listOf(empl2)),
            Pair(order3, listOf(empl1)),
        )

        assertResultMap(expectedMap, resultMap)
    }

    fun createEmployee(
        id: Long,
        sex: SexType,
        workStart: LocalTime,
        workFinish: LocalTime
    ): Employee {
        return Employee(
            id = id,
            firstName = "TEST",
            lastName = "TEST",
            middleName = "TEST",
            sex = sex,
            workStart = workStart,
            workFinish = workFinish,
            shiftType = "TEST",
            workPhone = "TEST",
            personalPhone = "TEST",
            employeeNumber = 123,
            lightDuties = false,
            rank = EmployeeRank("TEST", "TEST", "TEST", "TEST"),
            login = "TEST"
        )
    }

    fun createOrder(
        id: Long,
        startStationId: Long,
        finishStationId: Long,
        start: LocalDateTime,
        finish: LocalDateTime,
        femaleCount: Int,
        maleCount: Int,
        duration: Long
    ): Order {
        return Order(
            id = id,
            startDescription = "TEST",
            finishDescription = "TEST",
            orderApplication = null,
            duration = duration,
            passengerCount = 1,
            maleEmployeeCount = maleCount,
            femaleEmployeeCount = femaleCount,
            additionalInfo = "TEST",
            createdTime = LocalDateTime.MIN,
            orderTime = LocalDateTime.MIN,
            startTime = start,
            finishTime = finish,
            absenceTime = null,
            cancelTime = null,
            orderStatus = OrderStatusType.ACCEPTED,
            passenger = null,
            baggage = null,
            transfers = listOf(),
            passengerCategory = null,
            startStation = getMetroStation(startStationId),
            finishStation = getMetroStation(finishStationId),

            )
    }

    fun getMetroStation(
        id: Long
    ): MetroStation {
        return metroStationEntityRepository.findById(id)
            .map { metroStationMapper.entityToDomain(it) }
            .get()
    }

    fun assertResultMap(
        expectedMap: Map<Order, List<Employee>>,
        resultMap: Map<Order, List<Employee>>
    ) {
        expectedMap.forEach {
            val expectedEmplList = resultMap.get(it.key)

            assertEquals(expectedEmplList!!.size, it.value.size)
            expectedEmplList.containsAll(it.value)
            it.value.containsAll(expectedEmplList)
        }
    }

}