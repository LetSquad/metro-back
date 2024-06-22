package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.EmployeeShift
import ru.mosmetro.backend.model.dto.employee.EmployeeShiftDTO
import ru.mosmetro.backend.model.entity.EmployeeShiftEntity

@Component
class EmployeeShiftMapper(
    private val employeeMapper: EmployeeMapper,
) {
    fun entityToDomain(mapper: EmployeeShiftEntity) = EmployeeShift(
            id = mapper.id,
            shiftDate = mapper.shiftDate,
            workStart = mapper.workStart,
            workFinish = mapper.workFinish,
            employee = employeeMapper.entityToDomain(mapper.employee),
    )

    fun domainToDto(mapper: EmployeeShift) = EmployeeShiftDTO(
            id = mapper.id,
            shiftDate = mapper.shiftDate,
            workStart = mapper.workStart.plusHours(3),
            workFinish = mapper.workFinish.plusHours(3),
    )
}