package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.mapper.EmployeeMapper
import ru.mosmetro.backend.mapper.EmployeeShiftMapper
import ru.mosmetro.backend.mapper.EmployeeShiftOrderMapper
import ru.mosmetro.backend.model.domain.EmployeeShiftOrder
import ru.mosmetro.backend.model.domain.OrderTime
import ru.mosmetro.backend.model.dto.ListWithTotal
import ru.mosmetro.backend.model.dto.order.OrderTimeDTO
import ru.mosmetro.backend.model.dto.order.OrderTimeListDTO
import ru.mosmetro.backend.model.enums.TimeListActionType
import ru.mosmetro.backend.repository.EmployeeShiftEntityRepository
import ru.mosmetro.backend.repository.EmployeeShiftOrderEntityRepository
import ru.mosmetro.backend.util.MetroTimeUtil.METRO_TIME_FINISH
import ru.mosmetro.backend.util.MetroTimeUtil.METRO_TIME_START
import ru.mosmetro.backend.util.MetroTimeUtil.TIME_ZONE_UTC
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class TimeListService(
    val employeeShiftOrderEntityRepository: EmployeeShiftOrderEntityRepository,
    val employeeShiftEntityRepository: EmployeeShiftEntityRepository,
    val employeeMapper: EmployeeMapper,
    val employeeShiftMapper: EmployeeShiftMapper,
    val employeeShiftOrderMapper: EmployeeShiftOrderMapper,
) {

    fun addAllTime(
        timePlanDate: LocalDate,
        plan: List<OrderTime>
    ): List<OrderTime> {
       return plan.map {
            val actionNonWorkingTimeList = addNonWorkingTime(it.timePlan, it.employee.workStart, it.employee.workFinish, timePlanDate)
            val actionDownTimeList = addDownTimeTime(actionNonWorkingTimeList, it.employee.workStart, it.employee.workFinish, timePlanDate)

            OrderTime(
                employee = it.employee,
                timePlan = actionDownTimeList.toMutableList()
            )
        }
    }

    fun getOrderTimeListWithAllTime(
        timePlanDate: LocalDate
    ): ListWithTotal<OrderTimeDTO> {
        val instantDate = timePlanDate.atStartOfDay(TIME_ZONE_UTC).toInstant()

        val result: List<OrderTimeDTO> =
            employeeShiftEntityRepository.findAllByShiftDate(instantDate)
                .map { employeeShiftMapper.entityToDomain(it) }
                .map { employeeShift ->
                    val actionOrderTimeList =
                        employeeShiftOrderEntityRepository.findAllByEmployeeShiftId(employeeShift.id!!)
                            .filter { it.isAttached }
                            .map { employeeShiftOrderMapper.entityToDomain(it) }
                    val actionNonWorkingTimeList = addNonWorkingTime(actionOrderTimeList, employeeShift.workStart, employeeShift.workFinish, timePlanDate)
                    val actionDownTimeList = addDownTimeTime(actionNonWorkingTimeList,  employeeShift.workStart, employeeShift.workFinish, timePlanDate)

                    OrderTimeDTO(
                        employee = employeeShift.employee.let { employeeMapper.domainToDto(it) },
                        actions = actionDownTimeList.map { employeeShiftOrderMapper.domainToDto(it) }.sortedBy { it.timeStart }
                    )
                }

        return ListWithTotal(result.size, result)
    }

    fun getOrderTimeListWithAllTimeForTest(
        timePlanDate: LocalDate
    ): OrderTimeListDTO {
        val instantDate = timePlanDate.atStartOfDay(TIME_ZONE_UTC).toInstant()

        val result: List<OrderTimeDTO> =
            employeeShiftEntityRepository.findAllByShiftDate(instantDate)
                .map { employeeShiftMapper.entityToDomain(it) }
                .map { employeeShift ->
                    val actionOrderTimeList =
                        employeeShiftOrderEntityRepository.findAllByEmployeeShiftId(employeeShift.id!!)
                            .filter { it.isAttached }
                            .map { employeeShiftOrderMapper.entityToDomain(it) }
                    val actionNonWorkingTimeList = addNonWorkingTime(actionOrderTimeList, employeeShift.workStart, employeeShift.workFinish, timePlanDate)
                    val actionDownTimeList = addDownTimeTime(actionNonWorkingTimeList,  employeeShift.workStart, employeeShift.workFinish, timePlanDate)

                    OrderTimeDTO(
                        employee = employeeShift.employee.let { employeeMapper.domainToDto(it) },
                        actions = actionDownTimeList.map { employeeShiftOrderMapper.domainToDto(it) }.sortedBy { it.timeStart }
                    )
                }

        return OrderTimeListDTO(
            ordersNotInPlan = listOf(),
            ordersTime = result
        )
    }

    fun getOrderTimeList(
        timePlanDate: LocalDate
    ): List<OrderTime> {
        val instantDate = timePlanDate.atStartOfDay(TIME_ZONE_UTC).toInstant()

        return employeeShiftEntityRepository.findAllByShiftDate(instantDate)
            .map { employeeShiftMapper.entityToDomain(it) }
            .map { employeeShift ->
                val actionOrderTimeList: List<EmployeeShiftOrder> =
                    employeeShiftOrderEntityRepository.findAllByEmployeeShiftId(employeeShift.id!!)
                        .filter { it.isAttached }
                        .map { employeeShiftOrderMapper.entityToDomain(it) }

                OrderTime(
                    employee = employeeShift.employee,
                    timePlan = actionOrderTimeList.toMutableList()
                )
            }
    }

    private fun addNonWorkingTime(
        actions: List<EmployeeShiftOrder>,
        workStart: LocalTime,
        workFinish: LocalTime,
        date: LocalDate
    ): List<EmployeeShiftOrder> {
        val result: MutableList<EmployeeShiftOrder> = mutableListOf()
// 20 - 8
        if (workStart > workFinish) {
            result.add(
                EmployeeShiftOrder(
                    timeStart = LocalDateTime.of(date, METRO_TIME_START),
                    timeFinish = LocalDateTime.of(date, workStart),
                    actionType = TimeListActionType.NON_WORKING,
                    order = null
                )
            )
        } else {
            result.add(
                EmployeeShiftOrder(
                    timeStart = LocalDateTime.of(date, METRO_TIME_START),
                    timeFinish = LocalDateTime.of(date, workStart),
                    actionType = TimeListActionType.NON_WORKING,
                    order = null
                )
            )
            result.add(
                EmployeeShiftOrder(
                    timeStart = LocalDateTime.of(date, workFinish),
                    timeFinish = LocalDateTime.of(date.plusDays(1), METRO_TIME_FINISH),
                    actionType = TimeListActionType.NON_WORKING,
                    order = null
                )
            )
        }

        result.addAll(actions)

        return result
    }

    private fun addDownTimeTime(
        actions: List<EmployeeShiftOrder>,
        workStart: LocalTime,
        workFinish: LocalTime,
        date: LocalDate
    ): List<EmployeeShiftOrder> {
        val result: MutableList<EmployeeShiftOrder> = mutableListOf()
        val endWork = if (workStart > workFinish) LocalDateTime.of(date.plusDays(1), METRO_TIME_FINISH) else LocalDateTime.of(date, workFinish)
        actions
            .sortedBy { it.timeStart }
            .let {
                for ((index, value) in it.withIndex()) {
                    // первое время
                    if (index == 0) {
                        if (value.timeStart > LocalDateTime.of(date, METRO_TIME_START)) {
                            result.add(
                                EmployeeShiftOrder(
                                    timeStart = LocalDateTime.of(date, METRO_TIME_START),
                                    timeFinish = value.timeStart,
                                    actionType = TimeListActionType.DOWNTIME,
                                    order = null
                                )
                            )
                        }
                    }
                    // время в промежутке
                    if (index != it.size - 1) {
                        val next = it.get(index + 1)
                        if (value.timeFinish != next.timeStart) {
                            result.add(
                                EmployeeShiftOrder(
                                    timeStart = value.timeFinish,
                                    timeFinish = next.timeStart,
                                    actionType = TimeListActionType.DOWNTIME,
                                    order = null
                                )
                            )
                        }
                    // последнее время
                    } else if (value.timeFinish <= endWork) {
                        result.add(
                            EmployeeShiftOrder(
                                timeStart = value.timeFinish,
                                timeFinish = endWork,
                                actionType = TimeListActionType.DOWNTIME,
                                order = null
                            )
                        )
                    }

                    result.add(value)
                }
            }

        return result
    }

}
