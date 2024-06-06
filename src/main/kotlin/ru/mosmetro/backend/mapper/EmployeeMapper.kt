package ru.mosmetro.backend.mapper

import org.springframework.stereotype.Component
import ru.mosmetro.backend.model.domain.Employee
import ru.mosmetro.backend.model.dto.employee.EmployeeDTO
import ru.mosmetro.backend.model.dto.employee.NewEmployeeDTO
import ru.mosmetro.backend.model.dto.employee.UpdateEmployeeDTO
import ru.mosmetro.backend.model.entity.EmployeeEntity
import ru.mosmetro.backend.model.enums.SexType
import java.time.LocalTime

@Component
class EmployeeMapper(
        private val employeeRankMapper: EmployeeRankMapper
) {
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
        lightDuties = mapper.lightDuties,
        rank = employeeRankMapper.entityToDomain(mapper.rank)
    )

    fun domainToDto(mapper: Employee) = EmployeeDTO(
        id = mapper.id,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        shift = mapper.workStart.hour.toString() + ":" + mapper.workStart.hour.toString() + " - " + mapper.workFinish.hour.toString() + ":" + mapper.workFinish.hour.toString(),
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties,
        rank = employeeRankMapper.domainToDto(mapper.rank),
        employeeRole = employeeRankMapper.domainToDto(mapper.rank).role
    )

    fun dtoToDomain(mapper: NewEmployeeDTO) = Employee(
        id = null,
        firstName = mapper.firstName,
        lastName = mapper.lastName,
        middleName = mapper.middleName,
        sex = mapper.sex,
        workStart = LocalTime.of(mapper.shift.take(2).toInt(), mapper.shift.substring(4, 6).toInt()),
        workFinish = LocalTime.of(mapper.shift.substring(9, 11).toInt(), mapper.shift.takeLast(2).toInt()),
        shiftType = "", // TODO add map dictionary
        workPhone = mapper.workPhone,
        personalPhone = mapper.personalPhone,
        employeeNumber = mapper.employeeNumber,
        lightDuties = mapper.lightDuties,
        rank = TODO(),
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
        lightDuties = mapper.lightDuties,
        rank = TODO(),
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
        lightDuties = mapper.lightDuties,
        rank = TODO(),
    )
}