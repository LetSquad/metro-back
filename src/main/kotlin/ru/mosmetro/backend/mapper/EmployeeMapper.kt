package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.dto.employee.EmployeeDTO
import ru.mosmetro.backend.model.dto.employee.NewEmployeeDTO
import ru.mosmetro.backend.model.dto.employee.UpdateEmployeeDTO
import ru.mosmetro.backend.model.entity.EmployeeEntity
import ru.mosmetro.backend.model.enums.SexType

@Component
class EmployeeMapper {
    fun entityToDomain(mapper: EmployeeEntity) = Employee(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = SexType.valueOf(mapper.sex),
        workStart = mapper.workStart,
        workFinish = mapper.workFinish,
        shiftType = mapper.shiftType,
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties
    )

    fun domainToDto(mapper: Employee) = EmployeeDTO(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        workStart = mapper.workStart,
        workFinish = mapper.workFinish,
        shiftType = mapper.shiftType,
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties
    )

    fun dtoToDomain(mapper: NewEmployeeDTO) = Employee(
        id = null,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        workStart = mapper.workStart,
        workFinish = mapper.workFinish,
        shiftType = mapper.shiftType,
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties
    )

    fun dtoToDomain(mapper: UpdateEmployeeDTO, id: Long) = Employee(
        id = id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        workStart = mapper.workStart,
        workFinish = mapper.workFinish,
        shiftType = mapper.shiftType,
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties
    )

    fun domainToEntity(mapper: Employee) = EmployeeEntity(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex.name,
        workStart = mapper.workStart,
        workFinish = mapper.workFinish,
        shiftType = mapper.shiftType,
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties
    )
}