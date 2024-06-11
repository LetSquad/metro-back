package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.mapper.EmployeeMapper
import ru.mosmetro.backend.mapper.EmployeeShiftMapper
import ru.mosmetro.backend.mapper.EmployeeShiftOrderMapper
import ru.mosmetro.backend.model.domain.EmployeeShift
import ru.mosmetro.backend.model.domain.EmployeeShiftOrder
import ru.mosmetro.backend.model.dto.order.OrderTimeDTO
import ru.mosmetro.backend.model.dto.order.OrderTimeListDTO
import ru.mosmetro.backend.model.enums.TimeListActionType
import ru.mosmetro.backend.repository.EmployeeShiftEntityRepository
import ru.mosmetro.backend.repository.EmployeeShiftOrderEntityRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@Service
class TimeListService(
    val employeeShiftOrderEntityRepository: EmployeeShiftOrderEntityRepository,
    val employeeShiftEntityRepository: EmployeeShiftEntityRepository,
    val employeeMapper: EmployeeMapper,
    val employeeShiftMapper: EmployeeShiftMapper,
    val employeeShiftOrderMapper: EmployeeShiftOrderMapper,
) {

    // TODO пока без переноса дня работы, все в рамках одного дня
    fun getOrderTimeList(
        timePlanDate: LocalDate
    ): OrderTimeListDTO {
        val instantDate = timePlanDate.atStartOfDay(ZoneId.systemDefault()).toInstant()

        val result: List<OrderTimeDTO> =
            employeeShiftEntityRepository.findAllByShiftDate(instantDate)
                .map { employeeShiftMapper.entityToDomain(it) }
                .map { employeeShift ->
                    val actionOrderTimeList =
                        employeeShiftOrderEntityRepository.findAllByEmployeeShiftId(employeeShift.id!!)
                            .map { employeeShiftOrderMapper.entityToDomain(it) }
                    val actionNonWorkingTimeList = addNonWorkingTime(actionOrderTimeList, employeeShift, timePlanDate)
                    val actionDownTimeList = addDownTimeTime(actionNonWorkingTimeList, timePlanDate)

                    OrderTimeDTO(
                        employee = employeeShift.employee.let { employeeMapper.domainToDto(it) },
                        actions = actionDownTimeList.map { employeeShiftOrderMapper.domainToDto(it) }
                    )
                }

        return OrderTimeListDTO(result)
    }

    fun addNonWorkingTime(
        actions: List<EmployeeShiftOrder>,
        employeeShift: EmployeeShift,
        date: LocalDate
    ): List<EmployeeShiftOrder> {
        val result: MutableList<EmployeeShiftOrder> = mutableListOf()

        result.add(
            EmployeeShiftOrder(
                timeStart = LocalDateTime.of(date, METRO_TIME_START),
                timeFinish = LocalDateTime.of(date, employeeShift.workStart),
                actionType = TimeListActionType.NON_WORKING,
                order = null
            )
        )
        result.add(
            EmployeeShiftOrder(
                timeStart = LocalDateTime.of(date, employeeShift.workFinish),
                timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
                actionType = TimeListActionType.NON_WORKING,
                order = null
            )
        )
        result.addAll(actions)

        return result
    }

    fun addDownTimeTime(
        actions: List<EmployeeShiftOrder>,
        date: LocalDate
    ): List<EmployeeShiftOrder> {
        val result: MutableList<EmployeeShiftOrder> = mutableListOf()

        actions
            .sortedBy { it.timeStart }
            .let {
                for ((index, value) in it.withIndex()) {
                    // первое время
                    if (index == 0) {
                        if (value.timeStart.toLocalTime() > METRO_TIME_START) {
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
                    } else if (value.timeFinish.toLocalTime() < METRO_TIME_FINISH) {
                        result.add(
                            EmployeeShiftOrder(
                                timeStart = value.timeFinish,
                                timeFinish = LocalDateTime.of(date, METRO_TIME_FINISH),
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

    companion object {
        private val METRO_TIME_START = LocalTime.of(5, 30)
        private val METRO_TIME_FINISH = LocalTime.of(23, 59)
    }
}
